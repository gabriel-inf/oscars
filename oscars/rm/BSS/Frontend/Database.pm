# Database.pm:  BSS specific database settings and routines
#               inherits from Common::Database
# Last modified: July 8, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# Jason Lee (jrlee@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Database;
use Common::Exception;

our @ISA = qw(Common::Database);

##############################################################################
sub new {
    my $invocant = shift;
    my $_class = ref($invocant) || $invocant;
    my ($_self) = {@_};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

######

###############################################################################
sub logout {
    my( $self, $user_dn ) = @_;

    if (!$self->{oscars_logins}->{$user_dn}) {
        throw Common::Exception("Already logged out.");
    }
    if (!$self->{oscars_logins}->{$user_dn}->disconnect()) {
        throw Common::Exception("Could not disconnect from database");
    }
    if ($user_dn ne 'unpriv') {
        $self->{oscars_logins}->{$user_dn} = undef;
    }
    return({});
}
######

###############################################################################
# update_reservation: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_reservation {
    my ( $self, $login_dn, $inref, $status ) = @_;

    my ( $rref, $sth, $query );
    my $user_dn = $inref->{user_dn};

    $query = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    $sth = $self->do_query($login_dn, $query, $inref->{reservation_id});
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = @{$rref}[0]->{reservation_status};
    if ( ($prev_status eq 'precancel') || ( ($prev_status eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $query = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    $sth = $self->do_query($login_dn, $query, $status,
                           $inref->{reservation_id});
    $sth->finish();
    return( $status );
}
######

##############################################################################
#
sub get_reservation_id {
    my( $self, $user_dn ) = @_;

    return( $self->{oscars_logins}->{$user_dn}->{mysql_insertid} );
}
######

##############################################################################
#
sub get_trace_configs {
    my( $self ) = @_;

    my( $sth, $query );

        # use default for now
    $query = "SELECT " .
            "trace_conf_jnx_source, trace_conf_jnx_user, trace_conf_jnx_key, " .
            "trace_conf_ttl, trace_conf_timeout, " .
            "trace_conf_run_trace, trace_conf_use_system, " .
            "trace_conf_use_ping "  .
            "FROM trace_confs where trace_conf_id = 1";
        #TODO:  FIX!
    $sth = $self->do_query('', $query);
    my $configs = $sth->fetchrow_hashref();
    $sth->finish();
    return( $configs );
}
######

##############################################################################
#
sub get_pss_configs {
    my( $self ) = @_;

    my( $sth, $query );

        # use defaults for now
    $query = "SELECT " .
             "pss_conf_access, pss_conf_login, pss_conf_passwd, " .
             "pss_conf_firewall_marker, " .
             "pss_conf_setup_file, pss_conf_teardown_file, " .
             "pss_conf_ext_if_filter, pss_conf_CoS, " .
             "pss_conf_burst_limit, " .
             "pss_conf_setup_priority, pss_conf_resv_priority, " .
             "pss_conf_allow_lsp "  .
             "FROM pss_confs where pss_conf_id = 1";
        #TODO:  FIX!
    $sth = $self->do_query('', $query);
    my $configs = $sth->fetchrow_hashref();
    $sth->finish();
    return( $configs );
}
######

##############################################################################
# ip_to_xface_id:
#   Get the db iface id from an ip address.  Called from the scheduler to see
#   if a router is an edge router (will not be in ipaddrs table if it is not).
# In:  interface ip address
# Out: interface id
#
sub ip_to_xface_id {
    my ($self, $user_dn, $ipaddr) = @_;
    my ($query, $sth, $interface_id);

    $self->login_user($user_dn);
    $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddr_ip = ?';
    $sth = $self->do_query($user_dn, $query, $ipaddr);
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return( 0 );
    }
    my @data = $sth->fetchrow_array();
    $interface_id = $data[0];
    $sth->finish();
    return ($interface_id);
}
######

##############################################################################
# xface_id_to_loopback:  get the router name or loopback ip from the interface
#                        primary key.
# In:  interface table key id, and string, either 'name' or 'ip'
# Out: router name or loopback ip address
#
sub xface_id_to_loopback {
    my( $self, $user_dn, $interface_id, $which ) = @_;
    my( $query, $sth );

    $query = "SELECT router_name, router_loopback FROM routers
              WHERE router_id = (SELECT router_id from interfaces
                                 WHERE interface_id = ?)";
    $sth = $self->do_query($user_dn, $query, $interface_id);
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        # not considered an error
        return ("");
    }

    my @data = $sth->fetchrow_array();
    $sth->finish();
    if ($which eq 'name') { return ($data[0], ""); }

    # default, checks for loopback address
    if (!$data[1]) {
        throw Common::Exception("Router $data[0] has no oscars loopback");
    }
    return ($data[1]);
}
######

##############################################################################
# hostaddrs_ip_to_id:  get the primary key in the hostaddrs table, given a
#     host name or IP address (column names to be changed).
#     A row is created if that name or address is not present.
# In:  hostaddr_ip
# Out: hostaddr_id
#
sub hostaddrs_ip_to_id {
    my( $self, $user_dn, $ipaddr ) = @_;
    my( $query, $sth, $id );

    # TODO:  make hostaddr_ip field UNIQUE in hostaddrs?
    $query = 'SELECT hostaddr_id FROM hostaddrs WHERE hostaddr_ip = ?';
    $sth = $self->do_query($user_dn, $query, $ipaddr);
    # if no matches, insert a row in hostaddrs
    if ($sth->rows == 0 ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        $sth = $self->do_query($user_dn, $query);
        $id = $self->{oscars_logins}->{$user_dn}->{mysql_insertid};
    }
    else {
        my @data = $sth->fetchrow_array();
        $id = $data[0];
    }
    $sth->finish();
    return ($id);
}
######

##############################################################################
# hostaddrs_id_to_ip:  get the host name or IP address from the row in the
#                      hostaddrs table (names to be changed) identified by id.
# IN:  hostaddr_id
# OUT: hostaddr_ip
#
sub hostaddrs_id_to_ip {
    my( $self, $user_dn, $id ) = @_;
    my( $query, $sth, $ipaddr );

    $query = 'SELECT hostaddr_ip FROM hostaddrs WHERE hostaddr_id = ?';
    $sth = $self->do_query($user_dn, $query, $id);
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return( 0 );
    }
    my @data = $sth->fetchrow_array();
    $ipaddr = $data[0];
    $sth->finish();
    return( $ipaddr );
}
######

1;
# vim: et ts=4 sw=4
