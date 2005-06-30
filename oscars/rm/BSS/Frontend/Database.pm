# Database.pm:  BSS specific database settings and routines
#               inherits from Common::Database
# Last modified: June 30, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# Jason Lee (jrlee@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use DBI;
use Data::Dumper;

use Common::Database;
use AAAS::Client::SOAPClient;

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

    my $results = {};
    if (!$self->{handles}->{$user_dn}) {
        $results->{status_msg} = 'Already logged out.';
        return ( 0, $results );
    }
    if (!$self->{handles}->{$user_dn}->disconnect()) {
        $results->{error_msg} = "Could not disconnect from database";
        return ( 1, $results );
    }
    if ($user_dn ne 'unpriv') {
        $self->{handles}->{$user_dn} = undef;
    }
    $results->{status_msg} = 'Logged out';
    return ( 0, $results );
}
######

###############################################################################
# enforce_connection:  Checks to see if user has logged in by making a SOAP
#     call to the AAAS.
#
sub enforce_connection {
    my( $self, $user_dn ) = @_;

    my( %soap_params );

    $soap_params{user_dn} = $user_dn;
    my( $error_status, $results ) =
                AAAS::Client::SOAPClient::soap_check_login(\%soap_params);
    if ($results->{error_msg}) {
        print STDERR "soap_check_login error:  $results->{error_msg}\n";
        return $results->{error_msg};
    }

    # for now, handle set up per connection
    $results->{error_msg} = $self->login_user($user_dn);
    if ($results->{error_msg}) {
        print STDERR "login_user error:  $results->{error_msg}\n";
        return($results->{error_msg});
    }
    return "";
}
######

###############################################################################
# update_reservation: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_reservation {
    my ( $self, $login_dn, $inref, $status ) = @_;

    my ( $rref, $sth, $query );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $query = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    ($sth, $results->{error_msg}) = $self->do_query($login_dn, $query,
                                                    $inref->{reservation_id});
    if ( $results->{error_msg} ) { return( 1, $results ); }
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = @{$rref}[0]->{reservation_status};
    if ( ($prev_status eq $self->{configs}->{PENDING_CANCEL}) ||
         ( ($prev_status eq $self->{configs}->{PENDING}) &&
            ($status eq $self->{configs}->{PENDING_CANCEL}))) { 
        $status = $self->{configs}->{CANCELLED};
    }
    $query = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    ($sth, $results->{error_msg}) = $self->do_query($login_dn, $query, $status,
                                                    $inref->{reservation_id});
    if ( $results->{error_msg} ) { return( 1, $results ); }
    $sth->finish();
    $results->{status_msg} = "Successfully updated reservation.";
    return( 0, $results );
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
    my ($query, $sth, $interface_id, $error_msg);

    $error_msg = $self->enforce_connection($user_dn);
    if ( $error_msg ) {
        return( 0, $error_msg );
    }
    $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddr_ip = ?';
    ($sth, $error_msg) = $self->do_query($user_dn, $query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "");
    }
    my @data = $sth->fetchrow_array();
    $interface_id = $data[0];
    $sth->finish();
    return ($interface_id, "");
}
######

##############################################################################
# xface_id_to_loopback:  get the router name or loopback ip from the interface
#                        primary key.
# In:  interface table key id, and string, either 'name' or 'ip'
# Out: router name or loopback ip address
#
sub xface_id_to_loopback {
    my ($self, $user_dn, $interface_id, $which) = @_;
    my ($query, $sth, $error_msg);

    $query = "SELECT router_name, router_loopback FROM routers
              WHERE router_id = (SELECT router_id from interfaces
                                 WHERE interface_id = ?)";
    ($sth, $error_msg) = $self->do_query($user_dn, $query, $interface_id);
    if ( $error_msg ) {
        $sth->finish();
        return( "", $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        # not a fatal error
        return ("", "");
    }

    my @data = $sth->fetchrow_array();
    $sth->finish();
    if ($which eq 'name') { return ($data[0], ""); }

    # default, checks for loopback address
    if (!$data[1]) { return ('', "Router $data[0] has no oscars loopback"); }
    return ($data[1], "");
}
######

##############################################################################
# hostaddrs_ip_to_id:  get the primary key in the hostaddrs table, given a
#     host ip address.  A row is created if that ip address is not present.
# In:  hostaddr_ip
# Out: hostaddr_id
#
sub hostaddrs_ip_to_id {
    my ($self, $user_dn, $ipaddr) = @_;
    my ($query, $error_msg, $sth);
    my ($id);

    # TODO:  make hostaddr_ip field UNIQUE in hostaddrs?
    $query = 'SELECT hostaddr_id FROM hostaddrs WHERE hostaddr_ip = ?';
    ($sth, $error_msg) = $self->do_query($user_dn, $query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }

    # if no matches, insert a row in hostaddrs
    if ($sth->rows == 0 ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        ($sth, $error_msg) = $self->do_query($user_dn, $query);
        if ( $error_msg ) {
            $sth->finish();
            return( 0, $error_msg );
        }
        $id = $self->{handles}->{$user_dn}->{mysql_insertid};
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
# hostaddrs_id_to_ip:  get the ip address from the row in the hostaddrs table
#                      identified by the id.
# IN:  hostaddr_id
# OUT: hostaddr_ip
#
sub hostaddrs_id_to_ip {
    my ($self, $user_dn, $id) = @_;
    my ($query, $sth, $ipaddr, $error_msg);

    $query = 'SELECT hostaddr_ip FROM hostaddrs WHERE hostaddr_id = ?';
    ($sth, $error_msg) = $self->do_query($user_dn, $query, $id);
    if ( $error_msg ) {
        $sth->finish();
        return( 1, $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "");
    }
    my @data = $sth->fetchrow_array();
    $ipaddr = $data[0];
    $sth->finish();
    return ($ipaddr, "");
}
######

1;
# vim: et ts=4 sw=4
