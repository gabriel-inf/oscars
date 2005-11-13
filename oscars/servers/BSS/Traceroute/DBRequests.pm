# DBRequests.pm:  Database handling for BSS/Traceroute/RouteHandler.pm
# Last modified:  November 11, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

package BSS::Traceroute::DBRequests;

use strict;

use Data::Dumper;


###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

}
######

##############################################################################
#
sub get_trace_configs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT " .
            "trace_conf_jnx_source, trace_conf_jnx_user, trace_conf_jnx_key, " .
            "trace_conf_ttl, trace_conf_timeout, " .
            "trace_conf_run_trace, trace_conf_use_system, " .
            "trace_conf_use_ping "  .
            "FROM trace_confs where trace_conf_id = 1";
    my $configs = $self->{dbconn}->do_query($statement);
    return( $configs );
}
######

##############################################################################
# ip_to_xface_id:
#   Get the db iface id from an ip address. If a router is an edge router
#   there will be a corresponding address in the ipaddrs table.
# In:  interface ip address
# Out: interface id
#
sub ip_to_xface_id {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT interface_id FROM ipaddrs WHERE ipaddr_ip = ?';
    my $rows = $self->{dbconn}->do_query($statement, $ipaddr);
    if ( !@$rows ) {
        return( undef );
    }
    return( $rows->[0]->{interface_id} );
}
######

##############################################################################
# xface_id_to_loopback:  get the loopback ip from the interface primary key.
# In:  interface table primary key
# Out: loopback ip address
#
sub xface_id_to_loopback {
    my( $self, $interface_id ) = @_;

    my $statement = "SELECT router_name, router_loopback FROM routers
                 WHERE router_id = (SELECT router_id from interfaces
                                    WHERE interface_id = ?)";
    my $rows = $self->{dbconn}->do_query($statement, $interface_id);
    # no match
    if ( !@$rows ) {
        # not considered an error
        return( undef );
    }
    # check for loopback address
    if (!$rows->[0]->{router_loopback}) {
        throw Error::Simple("Router $rows->[0]->{router_name} has no oscars loopback");
    }
    return( $rows->[0]->{router_loopback} );
}
######

######
1;
