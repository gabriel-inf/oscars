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

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov),
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

February 28, 2006

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
    $self->{snmp_configs} = $self->get_snmp_configs();
} #____________________________________________________________________________


###############################################################################
# find_interface_ids:  Finds ingress and egress routers if they have not
#     already been specified, and gets the interface ids of the routers'
#     associated loopbacks.  It also checks to see if the next hop past the
#     egress router is in a domain running an OSCARS/BRUW server.
# IN:  OSCARS::Logger instance, and hash containing source name or IP,
#     destination name or IP, and ingress and egress router names or IP's,
#     if the user had permission to specify them
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub find_interface_ids {
    my( $self, $logger, $params) = @_;

    my( $path, $src, $dst, $next_domain );

    # converts to IP address if it is a host name
    $params->{source_ip} = $self->name_to_ip($params->{source_host});
    if( !$params->{source_ip} ) {
        throw Error::Simple("Unable to look up IP address of source");
    }
    $params->{destination_ip} = $self->name_to_ip($params->{destination_host});
    if( !$params->{destination_ip} ) {
        throw Error::Simple("Unable to look up IP address of destination");
    }
    #  If the egress router is given, use it as the source of the traceroute.
    #  Otherwise use the default router as the source.  The destination
    #  of the traceroute is the source given for the reservation.  The ingress 
    #  router chosen will be the router closest to the source that has
    #  a loopback.
    if ( $params->{egress_router} ) {
        # A straight copy if it is already an IP address.
        $src = $self->name_to_loopback( $params->{egress_router} );
    }
    else {
        $src =
           $self->name_to_ip( $self->{trace_configs}->{trace_conf_jnx_source} );
    }
    $dst = $params->{source_ip};
    $logger->add_string("--traceroute (reverse):  Source $src to destination $dst");
    # Run a traceroute and find the loopback IP, the associated interface,
    # and whether the next hop is an OSCARS/BRUW router.
    ( $path, $params->{ingress_ip}, $params->{ingress_interface_id}, $next_domain ) =
        $self->do_traceroute( $src, $params->{source_ip}, $logger );
    if( !$params->{ingress_interface_id} ) {
        throw Error::Simple("Ingress router $params->{ingress_router} has no loopback");
    }
  
    # Run a traceroute from the ingress_ip arrived at in the last step to the 
    # destination given in the reservation.
    $logger->add_string("--traceroute:  Source $src to destination $dst");
    ( $path, $params->{egress_ip}, $params->{egress_interface_id}, $next_domain ) =
        $self->do_traceroute(
            $params->{ingress_ip}, $params->{destination_ip}, $logger );
    if( !$params->{egress_interface_id} ) {
        throw Error::Simple("Egress router $params->{egress_router} has no loopback");
    }
    my $unused = pop(@$path);
    #$params->{next_domain} = $next_domain;
    $params->{reservation_path} = $path;
} #____________________________________________________________________________


###############################################################################
# do_traceroute:  Run traceroute from src to dst, find the last hop with a
#      loopback address, and find the domain name of the first hop outside
#      of the local domain.
# In:   source, destination IP addresses, OSCARS::Logger instance.
# Out:  IP of last hop within domain 
#
sub do_traceroute {
    my( $self, $src, $dst, $logger )  = @_;

    my $jnxTraceroute = new OSCARS::BSS::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst, $logger);
    my @hops = $jnxTraceroute->get_hops();
    my @path = ();

    # if we didn't hop much, maybe the same router?
    if ( $#hops < 0 ) { throw Error::Simple("same router?"); }

    if ( $#hops == 0) { return( $self->get_loopback( $src ), 'local' ); }

    # Loop through hops, identifying last local hop with a loopback, and
    # first hop outside local domain, if any, and its domain.
    my $interface_test = 1;
    my $interface_id = 0;
    my $loopback_test = 'l';
    my $loopback_ip = '';
    my $next_domain;
    for my $hop ( @hops )  {
        $logger->add_string("hop:  $hop");
        # following two are for edge router IP and interface id
        $loopback_test = $self->get_loopback( $hop );
        $interface_test = $self->get_interface( $hop );
        if ( $loopback_test ) {
            $loopback_ip = $loopback_test;
            $interface_id = $interface_test;
        }
        if ( $interface_test ) {
            push( @path, $interface_test );
        }
        else {
            #$next_domain = $self->get_domain($hop);
            last;
        }
    }
    return( \@path, $loopback_ip, $interface_id, $next_domain );
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
# name_to_loopback:  convert host name to loopback address if it isn't already 
#                    one (TODO:  error checking)
# In:   host name or loopback address 
# Out:  loopback address
#
sub name_to_loopback{
    my( $self, $name ) = @_;

    # last group is to handle CIDR blocks
    my $regexp = '\d+\.\d+\.\d+\.\d+(/\d+)*';
    if ($name !~ $regexp) { return( $self->get_loopback($name) ); }
    else { return $name; }
} #____________________________________________________________________________


###############################################################################
#
sub get_trace_configs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM BSS.trace_confs where trace_conf_id = 1";
    my $configs = $self->{user}->get_row($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub get_snmp_configs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM BSS.snmp_confs where snmp_conf_id = 1";
    my $configs = $self->{user}->get_row($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
# get_loopback:  Gets loopback address, if any, of router. 
# In:  interface ip address
# Out: loopback IP address, if any
#
sub get_loopback {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT router_loopback ' .
        'FROM BSS.routers b ' .
        'INNER JOIN BSS.interfaces i ON b.router_id = i.router_id ' .
        'INNER JOIN BSS.ipaddrs ip ON i.interface_id = ip.interface_id ' .
        'WHERE ip.ipaddr_ip = ?';
    my $row = $self->{user}->get_row($statement, $ipaddr);
    return( $row->{router_loopback} );
} #____________________________________________________________________________


###############################################################################
# get_interface:
#   Gets the interface id associated with an IP address.  Any address in the
#   local domain's topology database will have one.
# In:  interface ip address
# Out: interface id, if any
#
sub get_interface {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT interface_id FROM BSS.ipaddrs WHERE ipaddr_ip = ?';
    my $row = $self->{user}->get_row($statement, $ipaddr);
    return( $row->{interface_id} );
} #____________________________________________________________________________


###############################################################################
# get_domain:  gets the domain associated with an IP address
#                
# In:  IP address
# Out: domain_name (used by the AAAS to look up uri and proxy)
#
sub get_domain {
    my( $self, $ipaddr ) = @_;

    my $domain_name = 'local';

    return $domain_name;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
