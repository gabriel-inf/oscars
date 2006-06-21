#e==============================================================================
package OSCARS::Library::Topology::Pathfinder; 

=head1 NAME

OSCARS::Library::Topology::Pathfinder - Finds circuit path in local domain.

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

June 19, 2006

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

    $self->{jnxSnmp} = OSCARS::Library::Topology::JnxSNMP->new(
                                                     'db' => $self->{db} );
} #____________________________________________________________________________


###############################################################################
# findPathInfo:  Finds ingress and egress routers if they have not already been 
#     specified.  It also finds the autonomous system number of the next hop
#     past the local domain, if any.
# IN:  Hash containing source and destination of desired path, and optional
#     ingress and egress router names or IP's, if the user had permission to 
#     specify them
# OUT: IP's associated with edge routers, path lists (IP's), and next domain,
#      if any.
#
sub findPathInfo {
    my( $self, $params, $logger ) = @_;

    $self->{logger} = $logger;
    print STDERR Dumper($params);
    # make sure working with IP addresses
    my $srcHostIP = $self->nameToIP( $params->{srcHost} );
    my $ingressRouterIP =
               $params->{ingressRouterIP} ? $params->{ingressRouterIP} : undef;
    my $egressRouterIP =
               $params->{egressRouterIP} ? $params->{egressRouterIP} : undef;
    my $pathInfo = {};
    $pathInfo->{ingressRouterIP} = 
        $self->doReversePath( $srcHostIP, $ingressRouterIP, $egressRouterIP );

    my $destHostIP = $self->nameToIP( $params->{destHost} );
    $pathInfo->{srcIP} = $srcHostIP;
    $pathInfo->{destIP} = $destHostIP;
    # find path from ingress to reservation destination
    print STDERR "before doForwardPath\n";
    $pathInfo->{path} = $self->doForwardPath( $pathInfo->{ingressRouterIP},
                                                         $destHostIP );
    print STDERR "after doForwardPath\n";
    # find path strictly within this domain
    my( $localPath, $nextHop ) = $self->findLocalPath( $pathInfo->{path} );
    $pathInfo->{localPath} = $localPath;
    # if user specified egress router, use its loopback
    if ( $egressRouterIP ) {
    $pathInfo->{egressRouterIP} = $self->getRouterAddress( $egressRouterIP,
                                                         'loopback' ); 
    }
    # otherwise, use last hop with an interface
    else { $pathInfo->{egressRouterIP} = $pathInfo->{localPath}->[-1]; }

    my $nextAsNumber = $self->getAsNumber( $pathInfo->{localPath}->[-1],
                                      $nextHop );
    if ($nextAsNumber ne 'noSuchInstance') {
        if (!$params->{nextDomain} || ($params->{nextDomain}) != $nextAsNumber) {
            $pathInfo->{nextDomain} = $nextAsNumber;
        }
    }
    return $pathInfo;
} #____________________________________________________________________________


###############################################################################
# doReversePath:  Finds the loopback address of ingress router if it has 
#     not already been specified.
# IN:  hash containing relevant parameters.
# OUT: has containing loopback address of ingress router
#
sub doReversePath {
    my( $self, $srcHostIP, $ingressRouterIP, $egressRouterIP ) = @_;

    my( $src, $dest, $ingressLoopbackIP, $loopbackFound );

    # If the ingress router is given, just get the IP address associated with 
    # the loopback.
    if ( $ingressRouterIP ) {
        $ingressLoopbackIP = $self->getRouterAddress( $ingressRouterIP,
                                                  'loopback' );
        $self->{logger}->info('Pathfinder.ingress.specified',
            { 'router' => $ingressRouterIP } );
    }
    else {
        # Otherwise, if the egress router is given, use it as the source of 
        # a traceroute.
        if ( $egressRouterIP ) { $src = $egressRouterIP; }
        # The destination address of the traceroute is the source given for the 
        # reservation.
        $dest = $srcHostIP;

        $self->{logger}->info('Pathfinder.traceroute.reverse',
            { 'source' => $src, 'destination' => $dest } );
        # Run the traceroute and find all the hops.
        my $path = $self->doTraceroute( $src, $dest );
        print STDERR Dumper($path);
        # Loop through hops, identifying last hop with a loopback.  Note that 
        # an interface may be associated with an IP address without there also 
        # being a loopback.
        for my $hop ( @{$path} )  {
            print STDERR "hop: $hop\n";
            $loopbackFound = $self->getRouterAddress( $hop, 'loopback' );
        if ( $loopbackFound ) { $ingressLoopbackIP = $loopbackFound; }
        }
    }
    if( !$ingressLoopbackIP ) {
        # try source (hops don't include it)
        $ingressLoopbackIP = $self->getRouterAddress( $src, 'loopback' );
        if( !$ingressLoopbackIP ) {
            throw Error::Simple(
            "No router with loopback in (reverse) path from $src to $dest");
        }
    }
    print STDERR "ingress loopback: $ingressLoopbackIP\n";
    return $ingressLoopbackIP;
} #____________________________________________________________________________


###############################################################################
# doForwardPath:  Finds the forward path from ingress loopback IP to
#     reservation destination.
# IN:  hash containing relevant parameters
# OUT: forward path (array of IP's)
#
sub doForwardPath {
    my( $self, $ingressLoopbackIP, $destHostIP) = @_;

    # Traceroute is performed whether the egress router is specified or not. 
    # It is necessary to get the path within the local domain, and the 
    # autonomous system number of the next domain.
    $self->{logger}->info('Pathfinder.traceroute.forward',
            { 'source' => $ingressLoopbackIP,
              'destination' => $destHostIP } );
    return $self->doTraceroute( $ingressLoopbackIP, $destHostIP );
} #____________________________________________________________________________


###############################################################################
# doTraceroute:  Finds complete path between source and destination, converting
#     source and destination to IP addresses if necessary.
# IN:  source and destination, traceroute configuration variables
# OUT: path list (IP addresses)
#
sub doTraceroute {
    my( $self, $src, $destIP ) = @_;

    my $source;

    my $jnxTraceroute = new OSCARS::Library::Topology::JnxTraceroute(
                                                  'db' => $self->{db},
                                                  'logger' => $self->{logger} );
    if ( $src ) {
        $source = $self->getRouterAddress($src, 'traceAddress');
        if ( !$source ) { $source = $self->getRouterAddress($src, 'loopback'); }
    }
    if ( !$source ) {
        $source = 'default';
    }
    $jnxTraceroute->traceroute( $source, $destIP );
    my @path = $jnxTraceroute->getHops();
    # if we didn't hop much, maybe the same router?
    if ( $#path < 0 ) { throw Error::Simple("same router?"); }
    return \@path;
} #____________________________________________________________________________


###############################################################################
# findLocalPath:  Given a traceroute, find the path within the local domain
# In:   path between source and destination
# Out:  path within local domain (last hop in path is last hop with an 
#       interface)
#
sub findLocalPath {
    my( $self, $path )  = @_;

    my( $row, $interfaceFound, @interfacePath, $nextHop );

    my $statement = 'SELECT interfaceId FROM topology.ipaddrs WHERE IP = ?';
    for my $hop ( @{$path} )  {
        $row = $self->{db}->getRow($statement, $hop);
        if ( $row->{interfaceId} ) { 
            push( @interfacePath, $hop );
        }
        else {
            $nextHop = $hop;
        last; 
        }
    }
    return( \@interfacePath, $nextHop );
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
# getRouterAddress:  Gets loopback or trace IP address, if any, of router. 
# In:  IP address associated with router, and type (loopback or traceAddress)
# Out: loopback or trace IP address, if any
#
sub getRouterAddress {
    my( $self, $ipaddr, $addressType ) = @_;

    # first get router name
    my $statement = 'SELECT name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'INNER JOIN topology.ipaddrs ip ON i.id = ip.interfaceId ' .
        'WHERE ip.IP = ?';
    my $row = $self->{db}->getRow($statement, $ipaddr);
    if ( !$row->{name} ) { return undef; }

    # given router name, get address
    $statement = 'SELECT IP FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.interfaces i ON i.id = ip.interfaceId ' .
        'INNER JOIN topology.routers r ON r.id = i.routerId ' .
        "WHERE r.name = ? AND ip.description = '$addressType'";
    $row = $self->{db}->getRow($statement, $row->{name});
    return( $row->{IP} );
} #____________________________________________________________________________


###############################################################################
# getAsNumber:  Gets the autonomous service number associated with an IP 
#     address by performing an SNMP query against the egress router
#                
# In:  IP address of egress router, IP address of next hop
# Out: autonomous service number (used by the AAAS to look up uri and proxy)
#
sub getAsNumber {
    my( $self, $interfaceIP, $ipaddr ) = @_;

    my $asNumber;

    my $statement = 'SELECT name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'INNER JOIN topology.ipaddrs ip ON i.id = ip.interfaceId ' .
        'WHERE ip.IP = ?';
    my $row = $self->{db}->getRow($statement, $interfaceIP);
    $self->{jnxSnmp}->initializeSession( $row->{name} );
    my $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        throw Error::Simple("Unable to initialize SNMP session: $errorMsg");
    }
    $asNumber = $self->{jnxSnmp}->queryAsNumber($ipaddr);
    $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        #Log SNMP failure but build reservation up to this point
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $row->{name} , 'nextHop' => $ipaddr,
              'errorMessage' => $errorMsg });
        $asNumber = 'noSuchInstance';
    }
    $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $row->{name} , 'nextHop' => $ipaddr,
            'AS' => $asNumber });
    $self->{jnxSnmp}->closeSession();
    
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
