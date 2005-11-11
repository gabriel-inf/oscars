# RouteHandler.pm:  Finds ingress and egress router's and the path between
#                   them.
#
# Last modified:  November 10, 2005
# Jason Lee       (jrlee@lbl.gov)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Traceroute::RouteHandler; 

use Data::Dumper;
use Socket;
use Net::Ping;
use Net::Traceroute;
use Error qw(:try);

use Common::Exception;
use BSS::Traceroute::JnxTraceroute;
use BSS::Traceroute::DBRequests;

use strict;


##############################################################################
sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $self into designated class.
    bless($self, $class);
  
    # Initialize.
    $self->initialize();

    return($self);
}

sub initialize {
    my ($self) = @_;

    $self->{db_requests} = new BSS::Traceroute::DBRequests(
                                               'dbconn' => $self->{dbconn});
    $self->{trace_configs} = $self->{db_requests}->get_trace_configs()->[0];
    $self->{pss_configs} = $self->{dbconn}->get_pss_configs()->[0];
}
######

##############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host IP addresses
# OUT: ids of interfaces of the edge routers, path list (router indexes)
# TODO: validate input
#
sub find_interface_ids {
    my( $self, $src_host, $dst_host, $ingress_ip, $egress_ip) = @_;

    my( $path );

    print STDERR "$src_host, $dst_host, $ingress_ip, $egress_ip\n";
    $self->{output_buf} = "*********************\n";
    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ( !$ingress_ip ) {
        ($ingress_ip, $path) = $self->find_route_to_src( $src_host );
    }
    my $ingress_interface_id = $self->find_interface_id($ingress_ip);
    if (!$ingress_interface_id) {
        throw Common::Exception(
                         "Ingress loopback is not a valid OSCARS router");
    }
    print STDERR "Ingress loopback: $ingress_ip\n";

    if ( !$egress_ip ) {
        ($egress_ip, $path) = $self->find_route_to_dst($ingress_ip, $dst_host);
    }
    my $egress_interface_id = $self->find_interface_id($egress_ip);
    if (!$egress_interface_id) {
        throw Common::Exception(
                         "Egress loopback is not a valid OSCARS router");
    }
    print STDERR "Egress loopback: $egress_ip\n";
    return ( $ingress_interface_id, $egress_interface_id,
             $self->{output_buf} );
}
######

##############################################################################
# get_pss_fields:   get default PSS config fields used in reservation
#
sub get_pss_fields {
    my( $self ) = @_;

        # class of service
    my $reservation_class = $self->{pss_configs}->{pss_conf_CoS};
    my $reservation_burst_limit = $self->{pss_configs}->{pss_conf_burst_limit};
    return( $reservation_class, $reservation_burst_limit );
}
######

##############################################################################
# find_route_to_src:  use the default edge router to run a traceroute to the 
#     source to find the OSCARS loopback of the ingress router 
# IN:  source host
# OUT: ingress loopback IP address, list of routers in path
#
sub find_route_to_src {
    my( $self, $src_host ) = @_;

    my( $ingress_loopback, $path );

    # if source_host is a DNS name, convert to IP address
    my $ipaddr = $self->name_to_ip($src_host);
    if ( $self->{trace_configs}->{trace_conf_run_trace} )  {
        # add to log
        $self->{output_buf} .= "--traceroute:  " . 
            "$self->{trace_configs}->{trace_conf_jnx_source} to source " .
            "$ipaddr\n";
        # do traceroute
        ($ingress_loopback, $path) = $self->do_remote_trace(
                    $self->{trace_configs}->{trace_conf_jnx_source}, $ipaddr);
    }
    else {
        $self->{output_buf} .= "do_local_trace used\n";
        ($ingress_loopback, $path) = $self->do_local_trace( $ipaddr );
    }
    return ( $ingress_loopback, $path );
}
######

##############################################################################
# find_route_to_dst:  Run a traceroute from the ingress loopback found by
#     find_route_to_src to the destination host, and find the OSCARS
#     loopback for the egress router
# IN:  loopback IP of ingress router
# OUT: egress loopback IP address, list of routers in path
#
sub find_route_to_dst {
    my( $self, $loopback_ip, $dst_host ) = @_;

    my( $egress_loopback, $path );

    # if destination host is a DNS name, convert to IP address
    my $ipaddr = $self->name_to_ip($dst_host);
    if ( $self->{trace_configs}->{trace_conf_run_trace} )  {
        # add to log
        $self->{output_buf} .= "--traceroute:  " .
                               "$loopback_ip to destination $dst_host\n";
        ($egress_loopback, $path) = $self->do_remote_trace($loopback_ip,
                                                              $dst_host);
    }
    else {
        $self->{output_buf} .= "do_local_trace used\n";
        ($egress_loopback, $path) = $self->do_local_trace( $ipaddr );
    }
    return ( $egress_loopback, $path );
}
######

##############################################################################
# find_interface_id:  Find interface ID, if any, given loopback IP address
# IN:  loopback IP address
# OUT: interface ID
#
sub find_interface_id {
    my ($self, $ipaddr) = @_;
 
    my( $loopback );

    my $interface_id = $self->{db_requests}->ip_to_xface_id($ipaddr);
    if ($interface_id != 0) {
        $loopback = $self->{db_requests}->xface_id_to_loopback(
                                                         $interface_id, 'ip');
    }
    return ( $interface_id );
}
######

##############################################################################
# do_remote_trace:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path  
#
sub do_remote_trace {
    my ( $self, $src, $dst )  = @_;
    my (@hops);
    my ($interface_id, $prev_id, @path);
    my ($prev_loopback, $loopback_ip);

    @path = ();
    # try to ping before traceing?
    if ($self->{trace_configs}->{trace_conf_use_ping}) {
        $self->do_ping($dst);
    }
    my ($jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst);
    print STDERR "past traceroute\n";
    @hops = $jnxTraceroute->get_hops();
    print STDERR "past get_hops\n";

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Common::Exception("same router?"); }

    if ($#hops == 0) { 
        print STDERR "hops = 0\n";
            # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{db_requests}->ip_to_xface_id(
                            $self->{trace_configs}->{trace_conf_jnx_source});
        print STDERR "past ip_to_xface_id\n";
        $loopback_ip = $self->{db_requests}->xface_id_to_loopback($interface_id,
                                                              'ip');
        print STDERR "past xface_id_to_loopback\n";
        return ($interface_id, $loopback_ip, \@path);
    }

    # start off with an non-existent router
    $interface_id = 0;
    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    my $hop;
    for $hop (@hops)  {
        $self->{output_buf} .= "hop:  $hop\n";
        print STDERR "hop:  $hop\n";
        # id is 0 if not an edge router (not in interfaces table)
        print STDERR "in hops list\n";
        $interface_id = $self->{db_requests}->ip_to_xface_id($hop);
        print STDERR "to xface_id_to_loopback\n";
        $loopback_ip = $self->{db_requests}->xface_id_to_loopback( $interface_id,
                                                             'ip');
        print STDERR "past xface_id_to_loopback\n";
        if ($interface_id == 0) {
            $self->{output_buf} .= "edge router is $prev_loopback\n";
            print STDERR "edge router is $prev_loopback\n";
            return ($prev_id, $prev_loopback, \@path);
        }

        # add to the path
        push(@path, $interface_id);
        if ($loopback_ip && ($loopback_ip != 'NULL')) {
            $prev_id = $interface_id;
            $prev_loopback = $loopback_ip;
        }
    }
    # Need this in case the last hop is in the database
    if ($prev_loopback) {
        $self->{output_buf} .= "edge router is $prev_loopback\n";
        print STDERR "edge router is $prev_loopback\n";
        my $unused = pop(@path);
        return ($prev_id, $prev_loopback, \@path);
    }

    # if we didn't find it
    throw Common::Exception("Couldn't trace route to $src");
    return;
}
######

##############################################################################
# do_local_trace
#
sub do_local_trace {
    my ($self, $host)  = @_;
    my ($tr, $hops, $interface_id);

    # try to ping before traceing?
    if ($self->{trace_configs}->{trace_conf_use_ping}) { $self->do_ping($host); }
    $tr = new Net::Traceroute->new( host=>$host, timeout=>30, 
            query_timeout=>3, max_ttl=>20 ) || return (0);

    if( !$tr->found ) {
        throw Common::Exception("do_local_trace: $host not found");
    } 

    $hops = $tr->hops;
    $self->{output_buf} .= "do_local_trace:  hops = $hops\n";
    print STDERR "do_local_trace:  hops = $hops\n";

    # if we didn't hop much, mabe the same router?
    if ($hops < 2 ) {
        throw Common::Exception("do_local_trace: same router?");
    }

    # loop from the last router back, till we find an edge router
    for my $i (1..$hops-1) {
        my $ipaddr = $tr->hop_query_host($hops - $i, 0);
        $interface_id = $self->{db_requests}->ip_to_xface_id($ipaddr);
        if ($interface_id != 0) {
            $self->{output_buf} .= "do_local_trace:  edge router is $ipaddr\n";
            print STDERR "do_local_trace:  edge router is $ipaddr\n";
            return ($interface_id);
        } 
     }
    # if we didn't find it
    throw Common::Exception("do_local_trace:  could not find edge router");
    return;
}
######

##############################################################################
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
        throw Common::Exception("Host $host not pingable");
    # use the Net::Ping system
    } else {
        # make sure its up and pingable first
        my $p = Net::Ping->new(proto=>'icmp');
        if (! $p->ping($host, 5) )  {
            $p->close();
            throw Common::Exception("Host $host not pingable");
        }
        $p->close();
    }
    throw Common::Exception("Host $host not pingable");
}
######

##############################################################################
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
}
######

1;
# vim: et ts=4 sw=4
