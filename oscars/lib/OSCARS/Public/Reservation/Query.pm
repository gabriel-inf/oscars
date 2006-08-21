#==============================================================================
package OSCARS::Public::Reservation::Query;

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

OSCARS::Public::Reservation::Query- SOAP method to list the details of a
specific reservation.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Query;

=head1 DESCRIPTION

SOAP method that returns the details of a reservation
from the reservations table.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

July 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                                 'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  get reservation details from the database, given its
#     reservation tag.  If a user has the 'manage' permission on the
#     'Reservations' resource, they can query any reservation's details.
#     Otherwise they can only list reservations that they have made, with less
#     of the details.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my @strArray = split('-', $request->{tag});
    my $id = $strArray[-1];
    return $self->queryReservation($id);
} #____________________________________________________________________________


###############################################################################
# queryReservation:  get reservation details from the database, given 
#     its reservation id.  If a user has the proper authorization, he can view 
#     any reservation's details.  Otherwise he can only view reservations that
#     he has made, with less of the details.  If a database field is NULL
#     or blank, it is not returned.
#
# In:  reference to hash of parameters
# Out: reference to hash of reservation details
#
sub queryReservation {
    my( $self, $id ) = @_;

    my( $statement, $fields );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationAuthDetails WHERE id = ?';
        $fields = $self->{db}->getRow($statement, $id);
    }
    else {
        $statement = 'SELECT * FROM ReservationUserDetails ' .
                     'WHERE login = ? AND id = ?';
        $fields = $self->{db}->getRow($statement, $self->{user}->{login}, $id);
    }
    if (!$fields) { return undef; }
    my $resDetails = $self->{reservation}->format($fields);
    return $resDetails;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
