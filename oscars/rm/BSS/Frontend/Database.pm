# Database.pm:  BSS specific database settings and routines
# Last modified: May 18, 2005
# Jason Lee (jrlee@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use DBI;

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

sub initialize {
    my ( $_self ) = @_;
    my %attr = (
        RaiseError => 0,
        PrintError => 0
    );
    $_self->{dbh} = DBI->connect($_self->{configs}->{use_BSS_database}, 
             $_self->{configs}->{BSS_login_name}, $_self->{configs}->{BSS_login_passwd})
            or die "Couldn't connect to database: " . $DBI::errstr;
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
    my ($self, $ipaddr) = @_;
    my ($query, $sth, $interface_id, $error_msg);

    $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';
    ($sth, $error_msg) = $self->do_query($query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $ipaddr");
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
    my ($self, $interface_id, $which) = @_;
    my ($query, $sth, $error_msg);

    $query = "SELECT router_name, router_loopback FROM routers
              WHERE router_id = (SELECT router_id from interfaces
                                 WHERE interface_id = ?)";
    ($sth, $error_msg) = $self->do_query($query, $interface_id);
    if ( $error_msg ) {
        $sth->finish();
        return( "", $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return ("", "No match in database for $interface_id");
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
# In:  hostaddrs_ip
# Out: hostaddrs_id
#
sub hostaddrs_ip_to_id {
    my ($self, $ipaddr) = @_;
    my ($query, $error_msg, $sth);
    my ($id);

    # TODO:  make hostaddrs_ip field UNIQUE in hostaddrs?
    $query = 'SELECT hostaddrs_id FROM hostaddrs WHERE hostaddrs_ip = ?';
    ($sth, $error_msg) = $self->do_query($query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }

    # if no matches, insert a row in hostaddrs
    if ($sth->rows == 0 ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        ($sth, $error_msg) = $self->do_query($query);
        if ( $error_msg ) {
            $sth->finish();
            return( 0, $error_msg );
        }
        $id = $self->{dbh}->{mysql_insertid};
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
# IN:  hostaddrs_id
# OUT: hostaddrs_ip
#
sub hostaddrs_id_to_ip {
    my ($self, $id) = @_;
    my ($query, $sth, $ipaddr, $error_msg);

    $query = 'SELECT hostaddrs_ip FROM hostaddrs WHERE hostaddrs_id = ?';
    ($sth, $error_msg) = $self->do_query($query, $id);
    if ( $error_msg ) {
        $sth->finish();
        return( 1, $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $id");
    }
    my @data = $sth->fetchrow_array();
    $ipaddr = $data[0];
    $sth->finish();
    return ($ipaddr, "");
}
######

##############################################################################
#
sub do_query {
    my( $self, $query, @args ) = @_;
    my( $sth, $error_msg );

    $sth = $self->{dbh}->prepare( $query );
    if ($DBI::err) {
        $error_msg = "[DBERROR] Preparing $query:  $DBI::errstr";
        return (undef, $error_msg);
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        $error_msg = "[DBERROR] Executing $query:  $DBI::errstr";
        $sth->finish();
        return(undef, $error_msg);
    }
    return( $sth, '');
}
######

1;
# vim: et ts=4 sw=4
