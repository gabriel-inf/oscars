###############################################################################
package OSCARS::BSS::RouteHandler; 

# Finds ingress and egress routers and the path between them.
#
# Last modified:  December 7, 2005
# Jason Lee       (jrlee@lbl.gov)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;
use Socket;
use Net::Ping;
use Error qw(:try);

use strict;
use OSCARS::BSS::JnxTraceroute;
use OSCARS::BSS::Database;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{trace_configs} = $self->{dbconn}->get_trace_configs();
    $self->{pss_configs} = $self->{dbconn}->get_pss_configs();
} #____________________________________________________________________________ 


###############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host names or IP addresses, ingress and egress routers
#      if user specified
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub find_interface_ids {
    my( $self, $params) = @_;

    my( $loopback_ip, $path );

    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($params->{ingress_router}) {
        # converts to IP address if it is a host name
        $params->{ingress_ip} = $self->name_to_ip($params->{ingress_router});
        $params->{ingress_interface_id} = $self->{dbconn}->ip_to_xface_id( $params->{ingress_ip} );
        if ($params->{ingress_interface_id} != 0) {
            $loopback_ip =
                $self->{dbconn}->xface_id_to_loopback( $params->{ingress_interface_id} );
        }
        else {
            throw Error::Simple(
             "Ingress router $params->{ingress_router} does not have an OSCARS loopback");
        }
    }
    else {
        $params->{source_ip} = $self->name_to_ip($params->{source_host});
        $params->{logger}->write_log("--traceroute:  " .
             "$self->{trace_configs}->{trace_conf_jnx_source} to source $params->{source_ip}");
        ($params->{ingress_interface_id}, $loopback_ip, $path) =
            $self->do_traceroute('find_ingress', 
              $self->{trace_configs}->{trace_conf_jnx_source},
              $params->{source_ip}, $params->{logger});
    }
  
    if ($params->{egress_router}) {
        $params->{egress_ip} = $self->name_to_ip($params->{egress_router});
        $params->{egress_interface_id} =
            $self->{dbconn}->ip_to_xface_id( $params->{egress_ip} );
        if ($params->{egress_interface_id} != 0) {
            $loopback_ip =
                $self->{dbconn}->xface_id_to_loopback( $params->{egress_interface_id} );
        }
        else {
            throw Error::Simple(
             "Egress router $params->{egress_router} does not have an OSCARS loopback");
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        $params->{destination_ip} = $self->name_to_ip($params->{destination_host});
        $params->{logger}->write_log("--traceroute:  " .
                       "$loopback_ip to destination $params->{destination_ip}}");
        ($params->{egress_interface_id}, $loopback_ip, $params->{reservation_path}) =
            $self->do_traceroute('find_egress', $loopback_ip,
                               $params->{destination_ip}, $params->{logger});
    }
} #____________________________________________________________________________ 


###############################################################################
# get_pss_fields:   get default PSS config fields used in reservation
#
sub get_pss_fields {
    my( $self ) = @_;

        # class of service
    my $reservation_class = $self->{pss_configs}->{pss_conf_CoS};
    my $reservation_burst_limit = $self->{pss_configs}->{pss_conf_burst_limit};
    return( $reservation_class, $reservation_burst_limit );
} #____________________________________________________________________________ 


###############################################################################
# do_traceroute:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path  
#
sub do_traceroute {
    my ( $self, $action, $src, $dst, $logger )  = @_;
    my (@hops);
    my ($interface_id, $edge_id, @path);
    my ($edge_loopback, $loopback_ip);

    @path = ();
    # try to ping before traceing?
    if ($self->{trace_configs}->{trace_conf_use_ping}) { $self->do_ping($dst); }

    my ($jnxTraceroute) = new OSCARS::BSS::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst, $logger);
    @hops = $jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Error::Simple("same router?"); }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{dbconn}->ip_to_xface_id(
                              $self->{trace_configs}->{trace_conf_jnx_source});
        $loopback_ip =
            $self->{dbconn}->xface_id_to_loopback( $interface_id );
        return ($interface_id, $loopback_ip, \@path);
    }

    # start off with an non-existent router
    $interface_id = 0;
    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    my $hop;
    for $hop (@hops)  {
        $logger->write_log("hop:  $hop");
        # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{dbconn}->ip_to_xface_id( $hop );
        $loopback_ip =
            $self->{dbconn}->xface_id_to_loopback( $interface_id );
        if (!$interface_id) {
            $logger->write_log("edge router is $edge_loopback");
            return ($edge_id, $edge_loopback, \@path);
        }

        # add to the path
        push(@path, $interface_id);
        if (($action eq 'find_egress') || ($loopback_ip && ($loopback_ip != 'NULL'))) {
            $edge_id = $interface_id;
            $edge_loopback = $loopback_ip;
        }
    }
    # Need this in case the last hop is in the database
    if ($edge_loopback) {
        $logger->write_log("edge router is $edge_loopback");
        my $unused = pop(@path);
        return ($edge_id, $edge_loopback, \@path);
    }

    # if we didn't find it
    throw Error::Simple("Couldn't trace route to $src");
} #____________________________________________________________________________ 


###############################################################################
# do_ping:
# Freaking Net:Ping uses it own socket, so it has to be
# root to do icmp. Any smart person would turn off UDP and
# TCP echo ports ... gezzzz
#
sub do_ping {
    my ( $self, $host ) = @_;

    # use sytem 'ping' command (should be config'd in config file
    if ($self->{trace_configs}->{trace_conf_use_system}) {
        my @ping = `/bin/ping -w 10 -c 3 -n  $host`;
        for my $i (@ping) {
            if ( $i =~ /^64 bytes/ ) {  
                return; 
            }
        }
        throw Error::Simple("Host $host not pingable");
    # use the Net::Ping system
    } else {
        # make sure its up and pingable first
        my $p = Net::Ping->new(proto=>'icmp');
        if (! $p->ping($host, 5) )  {
            $p->close();
            throw Error::Simple("Host $host not pingable");
        }
        $p->close();
    }
    throw Error::Simple("Host $host not pingable");
} #____________________________________________________________________________ 


###############################################################################
# name_to_ip:  convert host name to IP address if it isn't already one
# In:   host name or IP
# Out:  host IP address
#
sub name_to_ip{
    my( $self, $host ) = @_;

    # last group is to handle CIDR blocks
    my $regexp = '\d+\.\d+\.\d+\.\d+(/\d+)*';
    if ($host !~ $regexp) { return( inet_ntoa(inet_aton($host)) ); }
    else { return $host; }
} #____________________________________________________________________________ 


######
1;
# vim: et ts=4 sw=4
