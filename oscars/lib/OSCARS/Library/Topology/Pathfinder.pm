#e==============================================================================
package OSCARS::Library::Topology::Pathfinder; 

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

July 3, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::Topology::JnxTraceroute;
use OSCARS::Library::Topology::Path;
use OSCARS::Library::Topology::Router;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{router} = OSCARS::Library::Topology::Router->new(
                            'db' => $self->{db}, 'logger' => $self->{logger} );
    $self->{path} = OSCARS::Library::Topology::Path->new(
                                                     'db' => $self->{db} );
    $self->{jnxTraceroute} = new OSCARS::Library::Topology::JnxTraceroute(
                            'db' => $self->{db}, 'logger' => $self->{logger} );
} #____________________________________________________________________________


###############################################################################
# getPath:  Finds local path from ingress to egress.  If one of these
#     has been specified by an admin, that overrules the default path.
#     It also finds the autonomous system number of the next hop
#     past the local domain, if any.
# IN:  Hash containing source and destination of desired path, and optional
#     ingress and egress router names or IP's, if the user had permission to 
#     specify them
# OUT: Hash containing source and destination host IP's, path list, and next 
#     domain, if any.
#
sub getPath {
    my( $self, $params ) = @_;

    my( $hops, $nextHop, $nextDomain );

    my $ingressRouterIP = $self->reversePath( $params );

    # If the egress router was specified by an admin.
    if ( $params->{egressRouterIP} ) {
        # Do a forward trace to get the local path
        ( $hops, $nextHop ) = $self->forwardPath( $ingressRouterIP,
                                                    $params->{egressRouterIP} );
        $self->{path}->setHops( $hops );
        # Do a forward trace from the egress to the destination to get the
        # next hop.
        ( $hops, $nextHop ) = $self->forwardPath( $params->{egressRouterIP},
                                                    $params->{destHost} );
    }
    # Else, trace from ingress to reservation destination, and get next hop if 
    # any.
    else {
        ( $hops, $nextHop ) = $self->forwardPath( $ingressRouterIP,
                                                    $params->{destHost} );
        $self->{path}->setHops( $hops );
    }
    my $nextAsNumber = $self->{router}->queryDomain( $hops->[-1], $nextHop );
    if ($nextAsNumber ne 'noSuchInstance') {
        if (!$params->{nextDomain} || 
           ($params->{nextDomain}) != $nextAsNumber) {
            $nextDomain = $nextAsNumber;
        }
    }
    return( $self->{path}, $nextDomain );
} #____________________________________________________________________________


###############################################################################
# reversePath:  Finds the ingress router if it has not already been 
#                 specified.
# IN:  hash containing relevant parameters.
# OUT: address of ingress router
#
sub reversePath {
    my( $self, $params ) = @_;

    my $src;

    # If the ingress router is given, make sure it is in the database,
    # and then return as is.
    if ( $params->{ingressRouterIP} ) {
        my $ingressLoopbackIP =
            $self->{router}->info( $params->{ingressRouterIP}, 'loopback' );
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
    my $hops = $self->traceroute( $src, $params->{srcHost} );
    # Find the last router in the database to get the ingress router.
    my $ingressRouterIP = $self->{path}->lastInterface( $hops );
    # Return the ingress loopback address (exception thrown if none).
    return $self->{path}->lastLoopback( $hops );
} #____________________________________________________________________________


###############################################################################
# forwardPath:  Finds the forward path from ingress router IP to
#     egress router IP.
# IN:  ingress router IP, destination IP, and hash to pass back to Create
# OUT: forward path (array of IP's)
#
sub forwardPath {
    my( $self, $src, $dest) = @_;

    # Traceroute is performed whether the egress router is specified or not. 
    # It is necessary to get the path within the local domain, and the 
    # autonomous system number of the next domain.
    $self->{logger}->info('Pathfinder.traceroute.forward',
                          { 'source' => $src, 'destination' => $dest } );
    my $hops = $self->traceroute( $src, $dest );
    my( $path, $nextHop ) = $self->{path}->nextHop( $hops );
    return( $path, $nextHop );
} #____________________________________________________________________________


###############################################################################
# traceroute:  Finds complete path between source and destination, converting
#     source and destination to IP addresses if necessary.
# IN:  source and destination, traceroute configuration variables
# OUT: path list (IP addresses)
#
sub traceroute {
    my( $self, $src, $dest ) = @_;

    my $source;

    if ( $src ) {
        $source = $self->{router}->info( $src, 'traceAddress' );
        if ( !$source ) { $source = $src; }
    }
    else { $source = 'default'; }
    my $pathSrc = $self->{jnxTraceroute}->traceroute( $source, $dest );
    my @hops = $self->{jnxTraceroute}->getHops();
    # prepend source to path
    unshift @hops, $pathSrc;
    # if we didn't hop much, maybe the same router?
    if ( $#hops < 0 ) { throw Error::Simple("same router?"); }
    return \@hops;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
