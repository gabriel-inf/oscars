# DBRequests.pm:  Database handling for BSS/Traceroute/RouteHandler.pm
# Last modified: November 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Traceroute::DBRequests;

use strict;

use DBI;
use Data::Dumper;

use BSS::Frontend::Database;

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;

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
    my ($interface_id);

    my $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddr_ip = ?';
    my $rows = $self->{dbconn}->do_query($query, $ipaddr);
    # no match
    if ( !$rows ) {
        return( 0 );
    }
    $interface_id = $rows->[0]->{interface_id};
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
    my( $self, $interface_id, $which ) = @_;

    my $query = "SELECT router_name, router_loopback FROM routers
                 WHERE router_id = (SELECT router_id from interfaces
                                    WHERE interface_id = ?)";
    my $rows = $self->{dbconn}->do_query($query, $interface_id);
    # no match
    # TODO:  fix rows
    if ( !$rows ) {
        # not considered an error
        return ("");
    }

    if ($which eq 'name') { return ($rows->[0]->{router_name}, ""); }

    # default, checks for loopback address
    if (!$rows->[0]->{router_loopback}) {
        throw Common::Exception("Router $rows->[0]->{router_name} has no oscars loopback");
    }
    return ($rows->[0]->{router_loopback});
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
    my( $self, $ipaddr ) = @_;
    my( $id );

    my $query = 'SELECT hostaddr_id FROM hostaddrs WHERE hostaddr_ip = ?';
    my $rows = $self->{dbconn}->do_query($query, $ipaddr);
    # if no matches, insert a row in hostaddrs
    if ( !$rows ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        my $unused = $self->{dbconn}->do_query($query);
        # TODO:  FIX mysql_insertid
        $id = $self->{dbh}->{mysql_insertid};
    }
    else {
        $id = $rows->[0]->{hostaddr_id};
    }
    return ($id);
}
######

