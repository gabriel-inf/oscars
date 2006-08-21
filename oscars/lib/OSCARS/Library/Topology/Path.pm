#==============================================================================
package OSCARS::Library::Topology::Path;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

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
    my $statement = 'SELECT id FROM topology.ipaddrs WHERE IP = ?';
    # get id for each hop in path
    for my $hop ( @{$self->{hops}} ) {
        $row = $self->{db}->getRow( $statement, $hop );  
        push( @ipaddrInfo, $row->{id} );
    }
    # build summary string for insertion
    my $pathStr = join(' ', @ipaddrInfo);
    # check for existing path
    $statement = 'SELECT id FROM topology.paths WHERE pathList = ?';
    my $pathList = $self->{db}->getRow( $statement, $pathStr );
    if ( $pathList->{id} ) { return $pathList->{id}; }

    # insert row into paths table
    $statement = qq{ INSERT INTO topology.paths VALUES ( NULL, 1, ?, 1 ) };
    $self->{db}->execStatement( $statement, $pathStr );
    my $pathId = $self->{db}->getPrimaryId();
    # for each hop, insert row into pathIpaddrs cross reference table
    $statement = qq{INSERT INTO topology.pathIpaddrs VALUES ( ?, ?, ? ) };
    my $ctr = 1;     # sequence number
    for my $id ( @ipaddrInfo ) {
        $self->{db}->execStatement( $statement, $pathId, $id, $ctr );
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
