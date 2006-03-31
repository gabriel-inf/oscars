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

March 31, 2006

=cut

use strict;

use Data::Dumper;
use Socket;
use Error qw(:try);

use OSCARS::BSS::JnxTraceroute;
use OSCARS::BSS::JnxSNMP;


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
    $self->{jnx_snmp} = OSCARS::BSS::JnxSNMP->new();
} #____________________________________________________________________________


###############################################################################
# find_interface_ids:  Finds ingress and egress routers if they have not
#     already been specified, and gets the interface ids of the routers'
#     associated loopbacks.  It also checks to see if the next hop past the
#     egress router is in a domain running an OSCARS/BRUW server.
# IN:  NetLogger instance, and hash containing source name or IP,
#     destination name or IP, and ingress and egress router names or IP's,
#     if the user had permission to specify them
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub find_interface_ids {
    my( $self, $logger, $params) = @_;

    my( $path, $src, $dst, $last_domain, $next_as_number );

    my $results = {};
    if ($params->{next_domain}) {
        $last_domain = $params->{next_domain};
    }
    # converts source to IP address if it is a host name
    $results->{source_ip} = $self->name_to_ip($params->{source_host}, 1);
    if( !$results->{source_ip} ) {
        throw Error::Simple("Unable to look up IP address of source");
    }
    $results->{destination_ip} =
            $self->name_to_ip($params->{destination_host}, 1);
    if( !$results->{destination_ip} ) {
        throw Error::Simple("Unable to look up IP address of destination");
    }

    #  If the ingress router is given, just get its loopback and interface id.
    #  Otherwise, if tthe egress router is given, use it as the source of the 
    #  traceroute.  Otherwise the default router is the source.  The destination
    #  of the traceroute is the source given for the reservation.  The ingress 
    #  router chosen will be the router closest to the source that has
    #  a loopback.
    if ( $params->{ingress_router} ) {
        $results->{ingress_ip} =
            $self->name_to_loopback($params->{ingress_router});
        $results->{ingress_interface_id} = $self->get_interface(
            $self->name_to_ip($params->{ingress_router}, 0));
    }
    else {
        if ( $params->{egress_router} ) {
            # A straight copy if it is already an IP address.
            $src = $self->name_to_loopback( $params->{egress_router} );
        }
        else {
            $src = $self->name_to_ip(
                        $self->{trace_configs}->{trace_conf_jnx_source}, 0 );
        }
        $dst = $results->{source_ip};
        $logger->info('traceroute.reverse',
            { 'source' => $src, 'destination' => $results->{source_ip} });
        # Run a traceroute and find the loopback IP, the associated interface,
        # and whether the next hop is an OSCARS/BRUW router.
        ( $path, $results->{ingress_ip}, $results->{ingress_interface_id}, $next_as_number ) =
            $self->do_traceroute( 'ingress', $src, $results->{source_ip}, $logger );
    }

    if( !$results->{ingress_interface_id} ) {
        throw Error::Simple("Ingress router $params->{ingress_router} has no loopback");
    }
  
    #  If the egress router is given, just get its loopback and interface id.
    #  Otherwise, run a traceroute from the ingress_ip arrived at in the last 
    #  step to the destination given in the reservation.
    if ( $params->{egress_router} ) {
        $results->{egress_ip} =
            $self->name_to_loopback($params->{egress_router});
        $results->{egress_interface_id} = $self->get_interface(
            $self->name_to_ip($params->{egress_router}, 0));
    }
    else {
        $logger->info('traceroute.forward',
            { 'source' => $results->{ingress_ip},
              'destination' => $results->{destination_ip} });
        ( $path, $results->{egress_ip}, $results->{egress_interface_id}, $next_as_number ) =
            $self->do_traceroute('egress',
                $results->{ingress_ip}, $results->{destination_ip}, $logger );
    }
    if( !$results->{egress_interface_id} ) {
        throw Error::Simple("Egress router $params->{egress_router} has no loopback");
    }
    my $unused = pop(@$path);
    if ($next_as_number ne 'noSuchInstance') {
        if ($last_domain != $next_as_number) {
            $results->{next_domain} = $next_as_number;
        }
        else { $results->{next_domain} = undef; }
    }
    $results->{reservation_path} = $path;
    return $results;
} #____________________________________________________________________________


###############################################################################
# do_traceroute:  Run traceroute from src to dst, find the last hop with a
#      loopback address, and find the autonomous service number of the first
#      hop outside of the local domain.
# In:   source, destination IP addresses, NetLogger instance.
# Out:  IP of last hop within domain 
#
sub do_traceroute {
    my( $self, $action, $src, $dst, $logger )  = @_;

    my $jnxTraceroute = new OSCARS::BSS::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{trace_configs}, $src, $dst, $logger);
    my @hops = $jnxTraceroute->get_hops();
    my @path = ();

    # if we didn't hop much, maybe the same router?
    if ( $#hops < 0 ) { throw Error::Simple("same router?"); }

    if ( $#hops == 0) { return( $self->get_loopback( $src ), 'local' ); }

    # Loop through hops, identifying last local hop with a loopback, and
    # first hop outside local domain, if any, and its autonomous service
    # number.  Note that an interface may be associated with an IP
    # address without there also being a loopback.
    my( $interface_found, $loopback_found, $interface_id, $loopback_ip );
    my $next_as_number;
    for my $hop ( @hops )  {
        $logger->info('traceroute.hop', {'hop' => $hop });
        # following two are for edge router IP and interface id
        $loopback_found = $self->get_loopback( $hop );
        $interface_found = $self->get_interface( $hop );
        if ( $loopback_found ) {
            $loopback_ip = $loopback_found;
            $interface_id = $interface_found;
        }
        if ( $interface_found ) { push( @path, $interface_found ); }
        elsif ($action eq 'egress') {
            $next_as_number = $self->get_as_number($interface_id, $hop);
            last;
        }
    }
    return( \@path, $loopback_ip, $interface_id, $next_as_number );
} #____________________________________________________________________________


###############################################################################
# name_to_ip:  convert host name to IP address if it isn't already one
# In:   host name or IP, and whether to keep CIDR portion if IP address
# Out:  host IP address
#
sub name_to_ip{
    my( $self, $host, $keep_cidr ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if doesn't match IP format, attempt to convert host name to IP address
    if ($host !~ $regexp) { return( inet_ntoa(inet_aton($host)) ); }
    elsif ($keep_cidr) { return $host; }
    else { return $1; }   # return IP address without CIDR suffix
} #____________________________________________________________________________


###############################################################################
# name_to_loopback:  convert host name to loopback address if it isn't already 
#                    one (TODO:  error checking)
# In:   host name or loopback address 
# Out:  loopback address
#
sub name_to_loopback{
    my( $self, $host ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if an IP address, get non-CIDR portion
    if ($host =~ $regexp) { $host = $1; }
    # else convert host name to IP address
    else { $host = $self->name_to_ip($host, 0); }
    return $self->get_loopback($host);
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
# Only used by tests.
#
sub get_test_configs {
    my( $self, $test_name ) = @_;

    my $statement = 'SELECT * FROM BSS.test_addresses a ' .
        'INNER JOIN BSS.test_confs t ON a.test_conf_id = t.test_conf_id ' .
        'WHERE t.test_name = ?';
    my $rows = $self->{user}->do_query($statement, $test_name);
    my $configs = {};
    for my $row (@$rows) {
        $configs->{$row->{test_address_description}} = $row->{test_address};
    }
    return $configs;
} #____________________________________________________________________________


###############################################################################
# get_loopback:  Gets loopback address, if any, of router. 
# In:  IP address (can't depend on being given host name, which would enable
#      a straight lookup out of the routers table)
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
# get_interface: Gets the interface id associated with an IP address.  Any 
#     address in the local domain's topology database will have one.
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
# get_as_number:  Gets the autonomous service number associated with an IP 
#     address by performing an SNMP query against the egress router
#                
# In:  Interface id on egress router, IP address of next hop
# Out: autonomous service number (used by the AAAS to look up uri and proxy)
#
sub get_as_number {
    my( $self, $interface_id, $ipaddr ) = @_;

    my $as_number;

    my $statement = 'SELECT router_name, domain_id FROM BSS.routers r ' .
        'INNER JOIN BSS.interfaces i ON r.router_id = i.router_id ' .
        'WHERE i.interface_id = ?';
    my $row = $self->{user}->get_row($statement, $interface_id);
    $statement = 'SELECT domain_suffix FROM BSS.domains ' .
                 'WHERE domain_id = ?';
    my $drow = $self->{user}->get_row($statement, $row->{domain_id});
    my $router_name = $row->{router_name} . $drow->{domain_suffix};
    $self->{jnx_snmp}->initialize_session($self->{snmp_configs}, $router_name);
    $as_number = $self->{jnx_snmp}->query_as_number($ipaddr);
    $self->{jnx_snmp}->close_session();
    return $as_number;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
