#==============================================================================
package OSCARS::Public::Reservation::Create;

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

OSCARS::Public::Reservation::Create - Handles creation of circuit reservation. 

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Create;

=head1 DESCRIPTION

SOAP method to create reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

July 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::Reservation;
use OSCARS::Library::Topology::Pathfinder;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                             'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles reservation creation. 
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $forwardResponse;

    $self->{pathfinder} = OSCARS::Library::Topology::Pathfinder->new(
                             'db' => $self->{db}, 'logger' => $logger );
    $logger->info("start", $request);
    # find path, and see if the next domain needs to be contacted
    my( $path, $nextDomain ) = $self->{pathfinder}->getPath( $request );
    $request->{path} = $path;     # save path for this domain
    # If nextDomain is set, forward checks to see if it is in the database,
    # and if so, forwards the request to the next domain.
    if ( $nextDomain ) {
        $request->{nextDomain} = $nextDomain;
        # TODO:  FIX (do copy here rather than in ClientForward
        $request->{ingressRouterIP} = undef;
        $request->{egressRouterIP} = undef;
        $forwardResponse =
             $self->{forwarder}->forward($request, $self->{configuration}, $logger);
    }
    # if successfuly found path, attempt to enter local domain's portion in db
    my $fields = $self->createReservation( $request );
    my $response = { 'status' => $fields->{status}, 'tag' => $fields->{tag} };
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


### Private methods. ###
 
###############################################################################
# createReservation:  builds row to insert into the reservations table,
#      checks for oversubscribed route, inserts the reservation, and
#      builds up the results to return to the client.
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub createReservation {
    my( $self, $request ) = @_;

    # Make sure no link is oversubscribed.
    $self->{reservation}->checkOversubscribed( $request );
    # Insert reservation in reservations table
    my $id = $self->{reservation}->insert( $request );
    # return status back, and tag if creation was successful
    my $statement = 'SELECT tag, status FROM ReservationUserDetails ' .
                    ' WHERE id = ?';
    return $self->{db}->getRow($statement, $id);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
