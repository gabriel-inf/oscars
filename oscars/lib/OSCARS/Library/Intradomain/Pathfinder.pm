#==============================================================================
package OSCARS::Intradomain::Pathfinder; 

=head1 NAME

OSCARS::Intradomain::Pathfinder - Finds ingress and egress routers.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Pathfinder;

=head1 DESCRIPTION

Performs traceroute from source to destination host, and given that, finds
the ingress and egress routers.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov),
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

April 18, 2006

=cut

use strict;

use Data::Dumper;
use Socket;
use Error qw(:try);

use OSCARS::Intradomain::JnxTraceroute;
use OSCARS::Intradomain::JnxSNMP;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{traceConfigs} = $self->getTraceConfigs();
    $self->{snmpConfigs} = $self->getSnmpConfigs();
    $self->{jnxSnmp} = OSCARS::Intradomain::JnxSNMP->new();
} #____________________________________________________________________________


###############################################################################
# findPath:  Finds ingress and egress routers if they have not
#     already been specified, and gets the interface ids of the routers'
#     associated loopbacks.  It also checks to see if the next hop past the
#     egress router is in a domain running an OSCARS/BRUW server.
# IN:  NetLogger instance, and hash containing source name or IP,
#     destination name or IP, and ingress and egress router names or IP's,
#     if the user had permission to specify them
# OUT: ids of interfaces of the edge routers, path list (router indexes)
#
sub findPath {
    my( $self, $logger, $params) = @_;

    my( $unusedPath, $src, $dst, $lastDomain, $nextAsNumber );

    my $results = {};
    if ($params->{nextDomain}) { $lastDomain = $params->{nextDomain}; }
    # converts source to IP address if it is a host name
    $results->{srcIP} = $self->nameToIP($params->{srcHost}, 1);
    if( !$results->{srcIP} ) {
        throw Error::Simple("Unable to look up IP address of source");
    }
    $results->{destIP} = $self->nameToIP($params->{destHost}, 1);
    if( !$results->{destIP} ) {
        throw Error::Simple("Unable to look up IP address of destination");
    }

    #  If the ingress router is given, just get its loopback and interface id.
    #  Otherwise, if tthe egress router is given, use it as the source of the 
    #  traceroute.  Otherwise the default router is the source.  The destination
    #  of the traceroute is the source given for the reservation.  The ingress 
    #  router chosen will be the router closest to the source that has
    #  a loopback.
    if ( $params->{ingressRouter} ) {
        $results->{ingressIP} =
            $self->nameToLoopback($params->{ingressRouter});
        $results->{ingressInterfaceId} = $self->getInterface(
            $self->nameToIP($params->{ingressRouter}, 0));
    }
    else {
        if ( $params->{egressRouter} ) {
            # A straight copy if it is already an IP address.
            $src = $self->nameToLoopback( $params->{egressRouter} );
        }
        else {
            $src = $self->nameToIP(
                        $self->{traceConfigs}->{jnxSource}, 0 );
        }
        $dst = $results->{srcIP};
        $logger->info('traceroute.reverse',
            { 'source' => $src, 'destination' => $results->{srcIP} });
        # Run a traceroute and find the loopback IP, the associated interface,
        # and whether the next hop is an OSCARS/BRUW router.
        ( $unusedPath,
          $results->{ingressIP}, $results->{ingressInterfaceId},
          $nextAsNumber ) = $self->doTraceroute( 'ingress',
                                       $src, $results->{srcIP}, $logger );
    }

    if( !$results->{ingressInterfaceId} ) {
        throw Error::Simple(
            "Ingress router $params->{ingressRouter} has no loopback");
    }
  
    #  If the egress router is given, just get its loopback and interface id.
    #  Otherwise, run a traceroute from the ingress IP arrived at in the last 
    #  step to the destination given in the reservation.
    if ( $params->{egressRouter} ) {
        $results->{egressIP} =
            $self->nameToLoopback($params->{egressRouter});
        $results->{egressInterfaceId} = $self->getInterface(
            $self->nameToIP($params->{egressRouter}, 0));
        # still have to do traceroute to get next hop and next domain
        my( $unusedIP, $unusedId );
        if ($results->{egressInterfaceId}) {
            ( $unusedPath, $unusedIP, 
              $unusedId, $nextAsNumber ) = $self->doTraceroute('egress',
                $results->{egressIP}, $results->{destIP}, $logger );
        }
    }
    else {
        $logger->info('traceroute.forward',
            { 'source' => $results->{ingressIP},
              'destination' => $results->{destIP} });
        ( $results->{path},
          $results->{egressIP},
          $results->{egressInterfaceId},
          $nextAsNumber ) = $self->doTraceroute('egress',
                $results->{ingressIP}, $results->{destIP}, $logger );
          my $unused = pop(@{$results->{path}});
    }
    if( !$results->{egressInterfaceId} ) {
        throw Error::Simple(
            "Egress router $params->{egressRouter} has no loopback");
    }
    if ($nextAsNumber ne 'noSuchInstance') {
        if ($lastDomain != $nextAsNumber) {
            $results->{nextDomain} = $nextAsNumber;
        }
        else { $results->{nextDomain} = undef; }
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# doTraceroute:  Run traceroute from src to dst, find the last hop with a
#      loopback address, and find the autonomous service number of the first
#      hop outside of the local domain.
# In:   source, destination IP addresses, NetLogger instance.
# Out:  IP of last hop within domain 
#
sub doTraceroute {
    my( $self, $action, $src, $dst, $logger )  = @_;

    my $jnxTraceroute = new OSCARS::Intradomain::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{traceConfigs}, $src, $dst, $logger);
    my @hops = $jnxTraceroute->getHops();
    my @path = ();

    # if we didn't hop much, maybe the same router?
    if ( $#hops < 0 ) { throw Error::Simple("same router?"); }

    if ( $#hops == 0) { return( $self->getLoopback( $src ), 'local' ); }

    # Loop through hops, identifying last local hop with a loopback, and
    # first hop outside local domain, if any, and its autonomous service
    # number.  Note that an interface may be associated with an IP
    # address without there also being a loopback.
    my( $interfaceFound, $loopbackFound, $interfaceId, $loopbackIP );
    my $nextAsNumber;

    # Get starting interface id, if any, in case next hop is outside of
    # domain.
    $loopbackFound = $self->getLoopback( $src );
    $interfaceFound = $self->getInterface( $src );
    if ( $loopbackFound ) {
        $loopbackIP = $loopbackFound;
        $interfaceId = $interfaceFound;
    }
    # Check starting point in case first hop is outside of domain
    for my $hop ( @hops )  {
        $logger->info('traceroute.hop', {'hop' => $hop });
        # following two are for edge router IP and interface id
        $loopbackFound = $self->getLoopback( $hop );
        $interfaceFound = $self->getInterface( $hop );
        if ( $loopbackFound ) {
            $loopbackIP = $loopbackFound;
            $interfaceId = $interfaceFound;
        }
        if ( $interfaceFound ) { push( @path, $interfaceFound ); }
        elsif ($action eq 'egress') {
            $nextAsNumber = $self->getAsNumber($interfaceId, $hop, $logger);
            last;
        }
    }
    return( \@path, $loopbackIP, $interfaceId, $nextAsNumber );
} #____________________________________________________________________________


###############################################################################
# nameToIP:  convert host name to IP address if it isn't already one
# In:   host name or IP, and whether to keep CIDR portion if IP address
# Out:  host IP address
#
sub nameToIP{
    my( $self, $host, $keepCidr ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if doesn't match IP format, attempt to convert host name to IP address
    if ($host !~ $regexp) { return( inet_ntoa(inet_aton($host)) ); }
    elsif ($keepCidr) { return $host; }
    else { return $1; }   # return IP address without CIDR suffix
} #____________________________________________________________________________


###############################################################################
# nameToLoopback:  convert host name to loopback address if it isn't already 
#                    one (TODO:  error checking)
# In:   host name or loopback address 
# Out:  loopback address
#
sub nameToLoopback{
    my( $self, $host ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if an IP address, get non-CIDR portion
    if ($host =~ $regexp) { $host = $1; }
    # else convert host name to IP address
    else { $host = $self->nameToIP($host, 0); }
    return $self->getLoopback($host);
} #____________________________________________________________________________


###############################################################################
#
sub getTraceConfigs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM traceConfs where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub getSnmpConfigs {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM snmpConfs where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
# getLoopback:  Gets loopback address, if any, of router. 
# In:  IP address (can't depend on being given host name, which would enable
#      a straight lookup out of the routers table)
# Out: loopback IP address, if any
#
sub getLoopback {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT loopback FROM routers r ' .
        'INNER JOIN interfaces i ON r.id = i.routerId ' .
        'INNER JOIN ipaddrs ip ON i.id = ip.interfaceId WHERE ip.IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return( $row->{loopback} );
} #____________________________________________________________________________


###############################################################################
# getInterface: Gets the interface id associated with an IP address.  Any 
#     address in the local domain's topology database will have one.
# In:  interface ip address
# Out: interface id, if any
#
sub getInterface {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT interfaceId FROM ipaddrs WHERE IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return( $row->{interfaceId} );
} #____________________________________________________________________________


###############################################################################
# getAsNumber:  Gets the autonomous service number associated with an IP 
#     address by performing an SNMP query against the egress router
#                
# In:  Interface id on egress router, IP address of next hop
# Out: autonomous service number (used by the AAAS to look up uri and proxy)
#
sub getAsNumber {
    my( $self, $interfaceId, $ipaddr, $logger ) = @_;

    my $asNumber;

    my $statement = 'SELECT name FROM routers r ' .
        'INNER JOIN interfaces i ON r.id = i.routerId WHERE i.id = ?';
    my $row = $self->{db}->getRow($statement, $interfaceId);
    my $routerName = $row->{name} .
                      $self->{snmpConfigs}->{domainSuffix};
    $self->{jnxSnmp}->initializeSession($self->{snmpConfigs}, $routerName);
    my $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        throw Error::Simple("Unable to initialize SNMP session: $errorMsg");
    }
    $asNumber = $self->{jnxSnmp}->queryAsNumber($ipaddr);
    $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        #throw Error::Simple("Unable to query $ipaddr for AS number: $errorMsg");
        
        #Log SNMP failure but build reservation up to this point
        $logger->info('Pathfinder.getAsNumber',
            { 'ip' => $ipaddr , 'errorMessage' => $errorMsg});
        $asNumber = 'noSuchInstance';
    }
    $self->{jnxSnmp}->closeSession();
    
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
