#==============================================================================
package OSCARS::BSS::RouteHandler; 

=head1 NAME

OSCARS::BSS::RouteHandler - Finds ingress and egress routers.

=head1 SYNOPSIS

  use OSCARS::BSS::RouteHandler;

=head1 DESCRIPTION

Performs traceroute from source to destination host, and given that, finds
the ingress and egress routers.

=head1 AUTHORS

Jason Lee (jrlee@lbl.gov),
David Robertson (dwrobertson@lbl.gov)
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

February 24, 2006

=cut

use strict;

use Data::Dumper;
use Socket;
use Error qw(:try);

use OSCARS::User;
use OSCARS::BSS::JnxTraceroute;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{trace_configs} = $self->get_trace_configs();
} #____________________________________________________________________________


###############################################################################
# find_interface_ids:  run traceroutes to both hosts.  Find edge routers and
# validate both ends.
# IN:  src and dst host names or IP addresses, ingress and egress routers
#      if user specified
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub find_interface_ids {
    my( $self, $logger, $params) = @_;

    my( $loopback_ip, $path );

    # If the loopbacks have not already been specified, use the default
    # router to run the traceroute to the source, and find the router with
    # an oscars loopback closest to the source 
    if ($params->{ingress_router}) {
        # converts to IP address if it is a host name
        $params->{ingress_ip} = $self->name_to_ip($params->{ingress_router});
        ($params->{ingress_interface_id}, $loopback_ip) = 
                $self->get_loopback( $params->{ingress_ip} );
        if( !$params->{ingress_interface_id} ) {
            throw Error::Simple("Router $params->{ingress_router} has no OSCARS loopback");
        }
    }
    else {
        $params->{source_ip} = $self->name_to_ip($params->{source_host});
        $logger->add_string("--traceroute:  " .
             "$self->{trace_configs}->{trace_conf_jnx_source} to source $params->{source_ip}");
        ($params->{ingress_interface_id}, $loopback_ip, $path) =
            $self->do_traceroute('find_ingress', 
              $self->{trace_configs}->{trace_conf_jnx_source},
              $params->{source_ip}, $logger, $params);
    }
  
    if ($params->{egress_router}) {
        $params->{egress_ip} = $self->name_to_ip($params->{egress_router});
        ($params->{egress_interface_id}, $loopback_ip) = 
                $self->get_loopback( $params->{egress_ip} );
        if( !$params->{egress_interface_id} ) {
            throw Error::Simple("Router $params->{egress_router} has no OSCARS loopback");
        }
    }
    else {
        # Use the address found in the last step to run the traceroute to the
        # destination, and find the egress.
        $params->{destination_ip} = $self->name_to_ip($params->{destination_host});
        $logger->add_string("--traceroute:  " .
                       "$loopback_ip to destination $params->{destination_ip}}");
        ($params->{egress_interface_id}, $loopback_ip, $params->{reservation_path}) =
            $self->do_traceroute('find_egress', $loopback_ip,
                               $params->{destination_ip}, $logger, $params);
    }
} #____________________________________________________________________________


###############################################################################
# do_traceroute:  Run traceroute from src to dst.
#
# In:   source, destination IP addresses.
# Out:  interface ID, path  
#
sub do_traceroute {
    my( $self, $action, $src, $dst, $logger, $params )  = @_;
    my( @hops );
    my( $interface_id, $edge_id, @path );
    my( $edge_loopback, $loopback_ip );

    @path = ();
    my ($jnxTraceroute) = new OSCARS::BSS::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst, $logger);
    @hops = $jnxTraceroute->get_hops();

    # if we didn't hop much, maybe the same router?
    if ($#hops < 0 ) { throw Error::Simple("same router?"); }

    if ($#hops == 0) { 
            # id is 0 if not an edge router (not in interfaces table)
        ($interface_id, $loopback_ip) = $self->get_loopback(
                              $self->{trace_configs}->{trace_conf_jnx_source});
        return ($interface_id, $loopback_ip, \@path);
    }

    # start off with an non-existent router
    $interface_id = 0;
    # loop forward till the next router isn't one of ours or doesn't have
    # an oscars loopback address
    my $hop;

    for $hop (@hops)  {
        $logger->add_string("hop:  $hop");
        # id is 0 if not an edge router (not in interfaces table)
        ( $interface_id, $loopback_ip )  = $self->get_loopback( $hop );
        if( !$interface_id ) {
            $logger->add_string("edge router is $edge_loopback");
            return ($edge_id, $edge_loopback, \@path);
        }
        my( $is_local, $domain_name ) =
                                 $self->check_domain( $interface_id );
           # pass back hops, through AAAS, to AAAS of server handling other
           # domain
        if( !$is_local ){ return (-1, $domain_name, \@path); }

        # add to the path
        push(@path, $interface_id);
        if (($action eq 'find_egress') || ($loopback_ip && ($loopback_ip != 'NULL'))) {
            $edge_id = $interface_id;
            $edge_loopback = $loopback_ip;
        }
    }
    # Need this in case the last hop corresponds to a router in the database
    if ($edge_loopback) {
        $logger->add_string("edge router is $edge_loopback");
        my $unused = pop(@path);
        return ($edge_id, $edge_loopback, \@path);
    }
    # if we didn't find it
    throw Error::Simple("Couldn't trace route to $src");
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


###############################################################################
#
sub get_trace_configs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT " .
            "trace_conf_jnx_source, trace_conf_jnx_user, trace_conf_jnx_key, " .
            "trace_conf_ttl, trace_conf_timeout, " .
            "trace_conf_run_trace " .
            "FROM BSS.trace_confs where trace_conf_id = 1";
    my $configs = $self->{user}->get_row($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
# get_loopback:
#   Gets the loopback IP, if any, given an IP address. If a router is an edge 
#   router, it will have a loopback address.
# In:  interface ip address
# Out: loopback IP address, if any
#
sub get_loopback {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT router_loopback, i.interface_id AS interface_id ' .
        'FROM BSS.routers b ' .
        'INNER JOIN BSS.interfaces i ON b.router_id = i.router_id ' .
        'INNER JOIN BSS.ipaddrs ip ON i.interface_id = ip.interface_id ' .
        'WHERE ip.ipaddr_ip = ?';
    my $statement = 'SELECT router_loopback, i.interface_id AS interface_id ' .
        'FROM BSS.routers b ' .
        'INNER JOIN BSS.interfaces i ON b.router_id = i.router_id ' .
        'INNER JOIN BSS.ipaddrs ip ON i.interface_id = ip.interface_id ' .
        'WHERE ip.ipaddr_ip = ?';
    my $row = $self->{user}->get_row($statement, $ipaddr);
    # not necessarily an error; up to caller about what to do
    if ( !$row ) { return( undef, undef ); }
    if (!$row->{router_loopback}) {
        throw Error::Simple("Router $row->{router_name} has no oscars loopback");
    }
    return ($row->{interface_id}, $row->{router_loopback});
} #____________________________________________________________________________


###############################################################################
# check_domain:  check whether router is on the local network
#                
# In:  interface primary key
# Out: whether in the local domain, and the domain's name (used to
#      look up uri and proxy by the AAAS)
#
sub check_domain {
    my( $self, $interface_id ) = @_;

    my $statement = 'SELECT domain_name, local_domain FROM BSS.domains d ' .
        'INNER JOIN BSS.routers r ON d.domain_id = r.domain_id ' .
        'INNER JOIN BSS.interfaces i ON r.router_id = i.router_id ' .
        'WHERE i.interface_id = ?';
    my $row = $self->{user}->get_row($statement, $interface_id);
    if ( !$row ) {
        throw Error::Simple("No domain associated with interface key $interface_id");
    }    
    return ($row->{local_domain}, $row->{domain_name});
} #____________________________________________________________________________ 

######
1;
# vim: et ts=4 sw=4
