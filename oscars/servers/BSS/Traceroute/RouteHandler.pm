# RouteHandler.pm:  Finds ingress and egress routers and the path between
#                   them.
#
# Last modified:  November 12, 2005
# Jason Lee       (jrlee@lbl.gov)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Traceroute::RouteHandler; 

use Data::Dumper;
use Socket;
use Net::Ping;
use Error qw(:try);

use BSS::Traceroute::JnxTraceroute;
use BSS::Traceroute::DBRequests;

use strict;


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

    $self->{db_requests} = new BSS::Traceroute::DBRequests(
                                               'dbconn' => $self->{dbconn});
    $self->{trace_configs} = $self->{db_requests}->get_trace_configs()->[0];
    $self->{pss_configs} = $self->{dbconn}->get_pss_configs()->[0];
}
######

##############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host names or IP addresses, ingress and egress routers
#      if user specified
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub find_interface_ids {
    my( $self, $inref) = @_;

    my( $loopback_ip, $path );

    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($inref->{ingress_router}) {
        # converts to IP address if it is a host name
        $inref->{ingress_ip} = $self->name_to_ip($inref->{ingress_router});
        $inref->{ingress_interface_id} = $self->{db_requests}->ip_to_xface_id( $inref->{ingress_ip} );
        if ($inref->{ingress_interface_id} != 0) {
            $loopback_ip =
                $self->{db_requests}->xface_id_to_loopback( $inref->{ingress_interface_id} );
        }
        else {
            throw Error::Simple(
             "Ingress router $inref->{ingress_router} does not have an OSCARS loopback");
        }
    }
    else {
        $inref->{source_ip} = $self->name_to_ip($inref->{source_host});
        print STDERR "--traceroute:  " .
             "$self->{trace_configs}->{trace_conf_jnx_source} to source $inref->{source_ip}\n";
        ($inref->{ingress_interface_id}, $loopback_ip, $path) =
            $self->do_traceroute(
              $self->{trace_configs}->{trace_conf_jnx_source}, $inref->{source_ip});
    }
  
    if ($inref->{egress_router}) {
        $inref->{egress_ip} = $self->name_to_ip($inref->{egress_router});
        $inref->{egress_interface_id} =
            $self->{db_requests}->ip_to_xface_id( $inref->{egress_ip} );
        if ($inref->{egress_interface_id} != 0) {
            $loopback_ip =
                $self->{db_requests}->xface_id_to_loopback( $inref->{egress_interface_id} );
        }
        else {
            throw Error::Simple(
             "Egress router $inref->{egress_router} does not have an OSCARS loopback");
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        $inref->{destination_ip} = $self->name_to_ip($inref->{destination_host});
        print STDERR "--traceroute:  " .
                       "$loopback_ip to destination $inref->{destination_ip}}\n";
        ($inref->{egress_interface_id}, $loopback_ip, $inref->{reservation_path}) =
            $self->do_traceroute($loopback_ip, $inref->{destination_ip});
    }
    return; 
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
# do remote trace:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path  
#
sub do_traceroute {
    my ( $self, $src, $dst )  = @_;
    my (@hops);
    my ($interface_id, $prev_id, @path);
    my ($prev_loopback, $loopback_ip);

    @path = ();
    # try to ping before traceing?
    if ($self->{trace_configs}->{trace_conf_use_ping}) { $self->do_ping($dst); }

    my ($jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst);
    @hops = $jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Error::Simple("same router?"); }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{db_requests}->ip_to_xface_id(
                              $self->{trace_configs}->{trace_conf_jnx_source});
        $loopback_ip =
            $self->{db_requests}->xface_id_to_loopback( $interface_id );
        return ($interface_id, $loopback_ip, \@path);
    }

    # start off with an non-existent router
    $interface_id = 0;
    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    my $hop;
    for $hop (@hops)  {
        print STDERR "hop:  $hop\n";
        # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{db_requests}->ip_to_xface_id( $hop );
        $loopback_ip =
            $self->{db_requests}->xface_id_to_loopback( $interface_id );
        if (!$interface_id) {
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
        print STDERR "edge router is $prev_loopback\n";
        my $unused = pop(@path);
        return ($prev_id, $prev_loopback, \@path);
    }

    # if we didn't find it
    throw Error::Simple("Couldn't trace route to $src");
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
