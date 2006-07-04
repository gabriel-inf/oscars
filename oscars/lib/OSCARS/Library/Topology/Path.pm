#==============================================================================
package OSCARS::Library::Topology::Path;

=head1 NAME

OSCARS::Library::Topology::Path - Encapsulates path handling

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Path;

=head1 DESCRIPTION

Handles functionality dealing with circuit paths.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::Topology::Router;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{router} = OSCARS::Library::Topology::Router->new(
                                                          'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
#
sub setHops {
    my( $self, $hops ) = @_;

    $self->{hops} = $hops;
} #____________________________________________________________________________


###############################################################################
#
sub getHops {
    my( $self ) = @_;

    return $self->{hops};
} #____________________________________________________________________________


###############################################################################
#
sub lastInterface {
    my( $self, $hops ) = @_;

    my $ingressRouterIP;

    for my $hop ( @{$hops} )  {
        my $interfaceFound = $self->{router}->interface( $hop );
        if ( $interfaceFound ) { $ingressRouterIP = $hop; }
    }
    if ( !$ingressRouterIP ) { 
        throw Error::Simple("No ingress interface found by reverse traceroute");
    }
    return $ingressRouterIP;
} #____________________________________________________________________________


###############################################################################
#
sub lastLoopback {
    my( $self, $hops ) = @_;

    my $ingressLoopbackIP;

    for my $hop ( @{$hops} )  {
        my $loopbackFound = $self->{router}->info( $hop, 'loopback' );
        if ( $loopbackFound ) { $ingressLoopbackIP = $hop; }
    }
    if ( !$ingressLoopbackIP ) { 
        throw Error::Simple("No ingress loopback found by reverse traceroute");
    }
    return $ingressLoopbackIP;
} #____________________________________________________________________________


###############################################################################
#
sub nextHop {
    my( $self, $hops ) = @_;

    my( $interfaceFound, @path, $outsideHop );

    for my $hop ( @{$hops} )  {
        $interfaceFound = $self->{router}->interface( $hop );
        if ( $interfaceFound ) { push( @path, $hop ); }
        else {
            $outsideHop = $hop;
            last; 
        }
    }
    return( \@path, $outsideHop );
} #____________________________________________________________________________


###############################################################################
#
sub insert {
    my( $self ) = @_;

    my( @ipaddrInfo, $row );

    # set up path information
    my $idStatement = 'SELECT id FROM topology.ipaddrs WHERE IP = ?';
    # get id for each hop in path
    for my $hop ( @{$self->{hops}} ) {
        $row = $self->{db}->getRow( $idStatement, $hop );  
        push( @ipaddrInfo, $row->{id} );
    }
    # build summary string for insertion
    my $pathStr = join(' ', @ipaddrInfo);
    # insert row into paths table (TODO:  check for duplicates)
    my $insertStatement = qq{ INSERT INTO topology.paths VALUES ( NULL, 1, ?, 1 ) };
    $self->{db}->execStatement( $insertStatement, $pathStr );
    my $pathId = $self->{db}->getPrimaryId();
    # for each hop, insert row into pathIpaddrs cross reference table
    $insertStatement = qq{INSERT INTO topology.pathIpaddrs VALUES ( ?, ?, ? ) };
    my $ctr = 1;     # sequence number
    for my $id ( @ipaddrInfo ) {
        $self->{db}->execStatement( $insertStatement, $pathId, $id, 
                                    $ctr );
        $ctr += 1;
    }
    return $pathId;
} #____________________________________________________________________________


###############################################################################
#
sub toString {
    my( $self, $pathId ) = @_;
 
    my $hops = $self->addresses($pathId);
    my @routerList = ();
    for my $hop ( @{$hops} ) {
        my $routerName = $self->{router}->name($hop->{IP});
        push(@routerList, $routerName); 
    }
    my $results = join( ' ', @routerList );
    return $results;
} #____________________________________________________________________________


###############################################################################
#
sub addresses {
    my( $self, $pathId, $addressType ) = @_;
 
    my $hops;

    my $statement = 'SELECT ip.IP, ip.description FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.pathIpaddrs pip ON pip.ipaddrId = ip.id  ' .
        'WHERE pip.pathId = ? ORDER BY sequenceNumber';
    if ( !$addressType ) {
        $hops = $self->{db}->doSelect($statement, $pathId);
    }
    elsif ( $addressType eq 'loopback' ) {
        my $rows = $self->{db}->doSelect($statement, $pathId);
        my @loopbacks = ();
        for my $row ( @{$rows} ) {
            my $loopback = $self->{router}->info( $row->{IP}, 'loopback' );
            if ( !$loopback ) { $loopback = $row->{IP}; }
	    else { $row->{description} = 'loopback'; }
            push( @loopbacks,
                 { 'IP' => $loopback, 'description' => $row->{description} } );
        }
        $hops = \@loopbacks;
    }
    return $hops;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
