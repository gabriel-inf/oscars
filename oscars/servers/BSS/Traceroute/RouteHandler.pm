# RouteHandler.pm:  Finds ingress and egress router's and the path between
#                   them.
#
# Last modified:  November 9, 2005
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
    $self->{configs} = $self->{db_requests}->get_trace_configs()->[0];
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
    my ($self, $inref) = @_;

    my( $ingress_interface_id, $egress_interface_id );
    my( $loopback_ip, $path, $start_router );

    $self->{output_buf} = "*********************\n";
    $self->convert_addresses($inref);

    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($inref->{ingress_ip}) {
        print STDERR "Ingress:  $inref->{ingress_ip}\n";
        $ingress_interface_id = $self->{db_requests}->ip_to_xface_id(
                                       $inref->{ingress_ip});
        if ($ingress_interface_id != 0) {
            $loopback_ip = $self->{db_requests}->xface_id_to_loopback(
                                       $ingress_interface_id, 'ip');
        }
        else {
            throw Common::Exception(
                             "Ingress loopback is not a valid OSCARS router");
        }
    }
    else {
        $self->{output_buf} .= "--traceroute:  " .
                              "$self->{configs}->{trace_conf_jnx_source} " .
                              "to source $inref->{source_ip}\n";
        ($ingress_interface_id, $loopback_ip, $path) =
                $self->do_remote_trace(
                              $self->{configs}->{trace_conf_jnx_source},
                              $inref->{source_ip});
        print STDERR "ingress:  past remote_trace\n";
    }
  
    if ($inref->{egress_ip}) {
        $egress_interface_id = $self->{db_requests}->ip_to_xface_id(
                                       $inref->{egress_ip});
        if ($egress_interface_id != 0) {
            $loopback_ip = $self->{db_requests}->xface_id_to_loopback(
                                      $egress_interface_id, 'ip');
        }
        else {
            throw Common::Exception(
                               "Egress loopback is not a valid OSCARS router");
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        if ( $self->{configs}->{trace_conf_run_trace} )  {
            $self->{output_buf} .= "--traceroute:  " .
                       "$loopback_ip to destination $inref->{destination_ip}\n";
            ($egress_interface_id, $loopback_ip, $path) =
                    $self->do_remote_trace($loopback_ip,
                                           $inref->{destination_ip});
        } else {
            $ingress_interface_id = $self->do_local_trace(
                                                      $inref->{source_ip});
        }
    }

    print STDERR "past the mess\n";
    if (($ingress_interface_id == 0) || ($egress_interface_id == 0)) {
        throw Common::Exception("Unable to find route.");
    }
    ($inref->{ingress_interface_id}, $inref->{egress_interface_id},
            $inref->{reservation_path}) = $self->find_interface_ids($inref);

    $inref->{ingress_interface_id} = $ingress_interface_id;
    $inref->{egress_interface_id} = $egress_interface_id;
    $inref->{reservation_path} = $path;
    print STDERR "to the end of find_interface_ids\n";
    return ( $self->{output_buf} );
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
    if ($self->{configs}->{trace_conf_use_ping}) { $self->do_ping($dst); }

    my ($jnxTraceroute) = new BSS::Traceroute::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{configs}, $src, $dst);
    print STDERR "past traceroute\n";
    @hops = $jnxTraceroute->get_hops();
    print STDERR "past get_hops\n";

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Common::Exception("same router?"); }

    if ($#hops == 0) { 
        print STDERR "hops = 0\n";
            # id is 0 if not an edge router (not in interfaces table)
        $interface_id = $self->{db_requests}->ip_to_xface_id(
                               $self->{configs}->{trace_conf_jnx_source});
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
    if ($self->{configs}->{trace_conf_use_ping}) { $self->do_ping($host); }
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
    if ($self->{configs}->{trace_conf_use_system}) {
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
# convert_addresses:  convert source and destination, and ingress and
# egress to IP's, if necessary.
sub convert_addresses{
    my( $self, $inref ) = @_;

    if (!($self->is_an_ip($inref->{source_host}))) {
        $inref->{source_ip} =
            inet_ntoa(inet_aton($inref->{source_host}));
    }
    else { $inref->{source_ip} = $inref->{source_host}; }
    if (!($self->is_an_ip($inref->{destination_host}))) {
        $inref->{destination_ip} =
            inet_ntoa(inet_aton($inref->{destination_host}));
    }
    else { $inref->{destination_ip} = $inref->{destination_host}; }

    if ($inref->{ingress_router}) {
        if (!($self->is_an_ip($inref->{ingress_router}))) {
            $inref->{ingress_ip} =
                inet_ntoa(inet_aton($inref->{ingress_router}));
        }
        else { $inref->{ingress_ip} = $inref->{ingress_router}; }
    }
    if ($inref->{egress_router}) {
        if (!($self->is_an_ip($inref->{egress_router}))) {
            $inref->{egress_ip} =
                 inet_ntoa(inet_aton($inref->{egress_router}));
        }
        else { $inref->{egress_ip} = $inref->{egress_router}; }
    }
}
######

################################################################################
sub is_an_ip {
    my( $self, $host ) = @_;

    my $regexp = '\d+\.\d+\.\d+\.\d+(/\d+)*';
    if ($host =~ $regexp) { return 1; }
    else { return 0; }
}
######

1;
# vim: et ts=4 sw=4
