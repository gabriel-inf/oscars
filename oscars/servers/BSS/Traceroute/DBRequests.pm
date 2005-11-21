###############################################################################
package BSS::Traceroute::DBRequests;

# Database requests made by Traceroute packages
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)


use strict;

use Data::Dumper;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


###############################################################################
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
    my $configs = $self->{dbconn}->get_row($statement);
    return $configs;
} #____________________________________________________________________________ 


###############################################################################
# ip_to_xface_id:
#   Get the db iface id from an ip address. If a router is an edge router
#   there will be a corresponding address in the ipaddrs table.
# In:  interface ip address
# Out: interface id
#
sub ip_to_xface_id {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT interface_id FROM ipaddrs WHERE ipaddr_ip = ?';
    my $row = $self->{dbconn}->get_row($statement, $ipaddr);
    if ( !$row ) { return undef; }
    return $row->{interface_id};
} #____________________________________________________________________________ 


###############################################################################
# xface_id_to_loopback:  get the loopback ip from the interface primary key.
# In:  interface table primary key
# Out: loopback ip address
#
sub xface_id_to_loopback {
    my( $self, $interface_id ) = @_;

    my $statement = "SELECT router_name, router_loopback FROM routers
                 WHERE router_id = (SELECT router_id from interfaces
                                    WHERE interface_id = ?)";
    my $row = $self->{dbconn}->get_row($statement, $interface_id);
    # it is not considered to be an error when no rows are returned
    if ( !$row ) { return undef; }
    # check for loopback address
    if (!$row->{router_loopback}) {
        throw Error::Simple("Router $row->{router_name} has no oscars loopback");
    }
    return $row->{router_loopback};
} #____________________________________________________________________________ 


######
1;
