#e==============================================================================
package OSCARS::Library::Topology::Pathfinder; 

=head1 NAME

OSCARS::Library::Topology::Pathfinder - Finds circuit path in local domain.

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Pathfinder;

=head1 DESCRIPTION

Performs traceroutes using reservation source and destination address, and/or
ingress and egress router IP's if they have been given, to find the path 
within the local domain from ingress to egress.  Makes sure that there is
an ingress loopback address, but doesn't return it.  Associated loopbacks, 
if any, are found using the path when returning results from reservation 
creation, and by the scheduler when it is time to set up an LSP.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov),
Andy Lake (arl10@albion.edu)

=head1 LAST MODIFIED

June 28, 2006

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
# findPathInfo:  Finds local path from ingress to egress.  If one of these
#     has been specified by an admin, that overrules the default path.
#     It also finds the autonomous system number of the next hop
#     past the local domain, if any.
# IN:  Hash containing source and destination of desired path, and optional
#     ingress and egress router names or IP's, if the user had permission to 
#     specify them
# OUT: Hash containing source and destination host IP's, path list, and next 
#     domain, if any.
#
sub findPathInfo {
    my( $self, $params, $logger ) = @_;

    my( $path, $nextHop );

    $self->{logger} = $logger;
    my $pathInfo = {};
    $pathInfo->{srcIP} = $self->{resvLib}->nameToIP($params->{srcHost});
    $pathInfo->{destIP} = $self->{resvLib}->nameToIP($params->{destHost});
    my $ingressRouterIP = $self->doReversePath( $params );

    # If the egress router was specified by an admin.
    if ( $params->{egressRouterIP} ) {
	# Do a forward trace to get the local path
        ( $path, $nextHop ) = $self->doForwardPath( $ingressRouterIP,
                                                    $params->{egressRouterIP} );
        $pathInfo->{path} = $path;
	# Do a forward trace from the egress to the destination to get the
	# next hop.
        ( $path, $nextHop ) = $self->doForwardPath( $params->{egressRouterIP},
                                                    $params->{destHost} );
    }
    # Else, trace from ingress to reservation destination, and get next hop if 
    # any.
    else {
        ( $path, $nextHop ) = $self->doForwardPath( $ingressRouterIP,
                                                    $params->{destHost} );
        $pathInfo->{path} = $path;
    }
    my $nextAsNumber = $self->getAsNumber( $pathInfo->{path}->[-1], $nextHop );
    if ($nextAsNumber ne 'noSuchInstance') {
        if (!$params->{nextDomain} || 
           ($params->{nextDomain}) != $nextAsNumber) {
            $pathInfo->{nextDomain} = $nextAsNumber;
        }
    }
    return $pathInfo;
} #____________________________________________________________________________


###############################################################################
# doReversePath:  Finds the ingress router if it has not already been 
#                 specified.
# IN:  hash containing relevant parameters.
# OUT: address of ingress router
#
sub doReversePath {
    my( $self, $params ) = @_;

    my( $src, $ingressRouterIP, $interfaceFound, $loopbackFound );
    my( $ingressLoopbackIP );

    # If the ingress router is given, make sure it is in the database,
    # and then return as is.
    if ( $params->{ingressRouterIP} ) {
        $ingressLoopbackIP =
            $self->{resvLib}->routerAddressType( $params->{ingressRouterIP},
	                                         'loopback' );
        if ( !$ingressLoopbackIP ) { 
            throw Error::Simple("No loopback for specified ingress router $params->{ingressRouterIP}");
        }
        $self->{logger}->info('Pathfinder.ingress.specified',
            { 'router' => $params->{ingressRouterIP} } );
        return $ingressLoopbackIP;
    }
    # Otherwise, if the egress router is given, use it as the source of 
    # the traceroute.
    if ( $params->{egressRouterIP} ) { $src = $params->{egressRouterIP}; }
    # Otherwise the default router is used as the source
    else { $src = 'default'; }

    $self->{logger}->info('Pathfinder.traceroute.reverse',
        { 'source' => $src,
          'destination' => $params->{srcHost} } );
    # Run the reverse traceroute to the reservation source.
    my $hops = $self->doTraceroute( $src, $params->{srcHost} );
    # Find the last router in the database to get the ingress router.
    for my $hop ( @{$hops} )  {
        $interfaceFound = $self->{resvLib}->getInterface( $hop );
        if ( $interfaceFound ) { $ingressRouterIP = $hop; }
    }
    if ( !$ingressRouterIP ) { 
        throw Error::Simple("No ingress interface found by reverse traceroute");
    }
    # Make sure the path has an ingress loopback address.
    for my $hop ( @{$hops} )  {
        $loopbackFound = $self->{resvLib}->routerAddressType( $hop, 'loopback' );
        if ( $loopbackFound ) { $ingressLoopbackIP = $hop; }
    }
    if ( !$ingressLoopbackIP ) { 
        throw Error::Simple("No ingress loopback found by reverse traceroute");
    }
    return $ingressLoopbackIP;
} #____________________________________________________________________________


###############################################################################
# doForwardPath:  Finds the forward path from ingress router IP to
#     egress router IP.
# IN:  ingress router IP, destination IP, and hash to pass back to Create
# OUT: forward path (array of IP's)
#
sub doForwardPath {
    my( $self, $src, $dest) = @_;

    my( $interfaceFound, @path, $nextHop );

    # Traceroute is performed whether the egress router is specified or not. 
    # It is necessary to get the path within the local domain, and the 
    # autonomous system number of the next domain.
    $self->{logger}->info('Pathfinder.traceroute.forward',
                          { 'source' => $src, 'destination' => $dest } );
    my $hops = $self->doTraceroute( $src, $dest );
    for my $hop ( @{$hops} )  {
        $interfaceFound = $self->{resvLib}->getInterface( $hop );
        if ( $interfaceFound ) { push( @path, $hop ); }
        else {
            $nextHop = $hop;
            last; 
        }
    }
    return( \@path, $nextHop );
} #____________________________________________________________________________


###############################################################################
# doTraceroute:  Finds complete path between source and destination, converting
#     source and destination to IP addresses if necessary.
# IN:  source and destination, traceroute configuration variables
# OUT: path list (IP addresses)
#
sub doTraceroute {
    my( $self, $src, $dest ) = @_;

    my $source;

    my $jnxTraceroute = new OSCARS::Library::Topology::JnxTraceroute(
                            'db' => $self->{db}, 'logger' => $self->{logger} );
    if ( $src ) {
        $source = $self->{resvLib}->routerAddressType( $src, 'traceAddress' );
        if ( !$source ) { $source = $src; }
    }
    else { $source = 'default'; }
    my $pathSrc = $jnxTraceroute->traceroute( $source, $dest );
    my @hops = $jnxTraceroute->getHops();
    # prepend source to path
    unshift @hops, $pathSrc;
    # if we didn't hop much, maybe the same router?
    if ( $#hops < 0 ) { throw Error::Simple("same router?"); }
    return \@hops;
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

    # Get router name for logging.  If unable to get, router not in db
    my $routerName = $self->{resvLib}->getRouterName($interfaceIP);
    if ( !$routerName ) {
        throw Error::Simple("Pathfinder.getAsNumber: no router in database for $interfaceIP");
    }
    $self->{jnxSnmp}->initializeSession( $interfaceIP );
    my $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        throw Error::Simple("Unable to initialize SNMP session: $errorMsg");
    }
    $asNumber = $self->{jnxSnmp}->queryAsNumber($ipaddr);
    $errorMsg = $self->{jnxSnmp}->getError();
    if ( $errorMsg ) {
        #Log SNMP failure but build reservation up to this point
        $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
              'errorMessage' => $errorMsg });
        $asNumber = 'noSuchInstance';
    }
    $self->{logger}->info('Pathfinder.getAsNumber',
            { 'router' => $routerName , 'nextHop' => $ipaddr,
            'AS' => $asNumber });
    $self->{jnxSnmp}->closeSession();
    
    return $asNumber;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
