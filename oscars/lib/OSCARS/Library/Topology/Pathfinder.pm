#e==============================================================================
package OSCARS::Library::Topology::Pathfinder; 

=head1 NAME

OSCARS::Library::Topology::Pathfinder - Finds ingress and egress routers.

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Pathfinder;

=head1 DESCRIPTION

Performs traceroute from source to destination host, and given that, finds
the ingress and egress routers.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov),
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

June 7, 2006

=cut

use strict;

use Data::Dumper;
use Socket;
use Error qw(:try);

use OSCARS::Library::Topology::JnxTraceroute;
use OSCARS::Library::Topology::JnxSNMP;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{traceConfigs} = $self->getTracerouteConfig();
    $self->{snmpConfigs} = $self->getSNMPConfiguration();
    $self->{pssConfigs} = $self->getPSSConfiguration();
    $self->{jnxSnmp} = OSCARS::Library::Topology::JnxSNMP->new();
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

    my $pathInfo = {};
    if ($params->{nextDomain}) { $lastDomain = $params->{nextDomain}; }
    # converts source to IP address if it is a host name
    $pathInfo->{srcIP} = $self->nameToIP($params->{srcHost}, 1);
    if( !$pathInfo->{srcIP} ) {
        throw Error::Simple("Unable to look up IP address of source");
    }
    $pathInfo->{destIP} = $self->nameToIP($params->{destHost}, 1);
    if( !$pathInfo->{destIP} ) {
        throw Error::Simple("Unable to look up IP address of destination");
    }

    #  If the ingress router is given, just get its loopback and interface id.
    #  Otherwise, if the egress router is given, use it as the source of the 
    #  traceroute.  Otherwise the default router is the source.  The destination
    #  of the traceroute is the source given for the reservation.  The ingress 
    #  router chosen will be the router closest to the source that has
    #  a loopback.
    if ( $params->{ingressRouterIP} ) {
        $pathInfo->{ingressLoopbackIP} =
            $self->nameToLoopback($params->{ingressRouterIP});
        $pathInfo->{ingressInterfaceId} = $self->getInterface(
            $self->nameToIP($params->{ingressRouterIP}, 0));
        $logger->info('Pathfinder.ingress',
            { 'router' => $params->{ingressRouterIP},
              'ingress' => 'specified',
	      'loopback' => $pathInfo->{ingressLoopbackIP} } );
    }
    else {
        if ( $params->{egressRouterIP} ) {
            # A straight copy if it is already an IP address.
            $src = $self->nameToLoopback( $params->{egressRouterIP} );
        }
        else {
            $src = $self->nameToIP(
                        $self->{traceConfigs}->{jnxSource}, 0 );
        }
        $dst = $pathInfo->{srcIP};
        $logger->info('Pathfinder.traceroute.reverse',
            { 'source' => $src,
              'destination' => $pathInfo->{srcIP},
              'ingress' => 'unspecified' } );
        # Run a traceroute and find the loopback IP, the associated interface,
        # and whether the next hop is an OSCARS/BRUW router.
        ( $unusedPath,
          $pathInfo->{ingressLoopbackIP}, $pathInfo->{ingressInterfaceId},
          $nextAsNumber ) = $self->doTraceroute( 'ingress',
                                       $src, $pathInfo->{srcIP}, $logger );
    }

    if( !$pathInfo->{ingressInterfaceId} ) {
        throw Error::Simple(
            "Ingress router $params->{ingressRouterIP} has no loopback");
    }
  
    #  If the egress router is given, just get its loopback and interface id.
    #  Otherwise, run a traceroute from the ingress IP arrived at in the last 
    #  step to the destination given in the reservation.
    if ( $params->{egressRouterIP} ) {
        $pathInfo->{egressLoopbackIP} =
            $self->nameToLoopback($params->{egressRouterIP});
        $pathInfo->{egressInterfaceId} = $self->getInterface(
            $self->nameToIP($params->{egressRouterIP}, 0));
        # still have to do traceroute to get next hop and next domain
        my( $unusedIP, $unusedId );
        $logger->info('Pathfinder.traceroute.forward',
            { 'source' => $pathInfo->{ingressLoopbackIP},
              'destination' => $pathInfo->{destIP},
	      'loopback' => $params->{egressLoopbackIP},
	      'router' => $params->{egressRouterIP},
              'egress' => 'specified' } );
        if ($pathInfo->{egressInterfaceId}) {
            ( $pathInfo->{path},
              $unusedIP, 
              $unusedId,
	      $nextAsNumber ) = $self->doTraceroute('egress',
                $pathInfo->{ingressLoopbackIP}, $pathInfo->{destIP}, $logger );
        }
    }
    else {
        $logger->info('Pathfinder.traceroute.forward',
            { 'source' => $pathInfo->{ingressLoopbackIP},
              'destination' => $pathInfo->{destIP},
              'egress' => 'unspecified' } );
        ( $pathInfo->{path},
          $pathInfo->{egressLoopbackIP},
          $pathInfo->{egressInterfaceId},
          $nextAsNumber ) = $self->doTraceroute('egress',
                $pathInfo->{ingressLoopbackIP}, $pathInfo->{destIP}, $logger );
          my $unused = pop(@{$pathInfo->{path}});
    }
    if( !$pathInfo->{egressInterfaceId} ) {
        throw Error::Simple(
            "Egress router $params->{egressRouterIP} has no loopback");
    }
    if ($nextAsNumber ne 'noSuchInstance') {
        if ($lastDomain != $nextAsNumber) {
            $pathInfo->{nextDomain} = $nextAsNumber;
        }
        else { $pathInfo->{nextDomain} = undef; }
    }
    $logger->info('Pathfinder.pathInfo', $pathInfo);
    return( $pathInfo, $self->{pssConfigs} );
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

    my $jnxTraceroute = new OSCARS::Library::Topology::JnxTraceroute();
    $jnxTraceroute->traceroute($self->{traceConfigs}, $self->getTraceAddress($src), $dst, $logger);
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
        # following two are for edge router IP and interface id
        $loopbackFound = $self->getLoopback( $hop );
        $interfaceFound = $self->getInterface( $hop );

        if ( $loopbackFound ) {
            $loopbackIP = $loopbackFound;
            $interfaceId = $interfaceFound;
        } elsif ( $interfaceFound ) { 
            push( @path, $interfaceFound ); 
	    if ($action eq 'egress') {
	        $interfaceId = $interfaceFound;
	    }
        } elsif ($action eq 'egress') {
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
sub getTracerouteConfig {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM topology.configTrace where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub getSNMPConfiguration {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM topology.configSNMP where id = 1";
    my $configs = $self->{db}->getRow($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub getPSSConfiguration {
    my( $self ) = @_;

        # use default for now
    my $statement = "SELECT * FROM topology.configPSS where id = 1";
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

    my $statement = 'SELECT loopback FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'INNER JOIN topology.ipaddrs ip ON i.id = ip.interfaceId ' .
        'WHERE ip.IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    return( $row->{loopback} );
} #____________________________________________________________________________

###############################################################################
# getTraceAddress:  Gets address from which to run traceroute for a given router.
# If none specified it returns the loopback
# In:  Loopback address of router from which traceroute needs to be run
# Out: Traceroute address, if any, otherwise the loopback
#
sub getTraceAddress {
    my ($self, $loopback) = @_;
	
    my $statement = 'SELECT traceAddress FROM topology.routers' .
        ' WHERE routers.loopback = ?';
    my $row = $self->{db}->getRow($statement, $loopback);
    return( $row->{traceAddress} ? $row->{traceAddress} : $loopback );
} #____________________________________________________________________________

###############################################################################
# getInterface: Gets the interface id associated with an IP address.  Any 
#     address in the local domain's topology database will have one.
# In:  interface ip address
# Out: interface id, if any
#
sub getInterface {
    my ($self, $ipaddr) = @_;

    my $statement = 'SELECT interfaceId FROM topology.ipaddrs WHERE IP = ?';
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

    my $statement = 'SELECT name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId WHERE i.id = ?';
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
        #Log SNMP failure but build reservation up to this point
        $logger->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
              'errorMessage' => $errorMsg });
        $asNumber = 'noSuchInstance';
    }
    $logger->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
	      'AS' => $asNumber });
    $self->{jnxSnmp}->closeSession();
    
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
