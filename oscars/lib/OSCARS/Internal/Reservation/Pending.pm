#==============================================================================
package OSCARS::Internal::Reservation::Pending;

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

OSCARS::Internal::Reservation::Pending - SOAP method to find pending 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Pending;

=head1 DESCRIPTION

SOAP method to find pending OSCARS reservations.  It calls the PSS to setup 
a label-switched path for pending reservations.  Inherits from OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

May 2, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Internal::Reservation::Scheduler;
our @ISA = qw{OSCARS::Internal::Reservation::Scheduler};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{opcode} = $self->{LSP_SETUP};
    $self->{opstring} = 'setup';
} #____________________________________________________________________________


###############################################################################
#
sub getReservations  { 
    my ( $self, $timeInterval ) = @_;

    my $status = 'pending';
    my $statement = "SELECT unix_timestamp() AS nowTime";
    my $row = $self->{db}->getRow( $statement );
    my $timeslot = $row->{nowTime} + $timeInterval;
    $statement = qq{ SELECT * FROM ReservationAuthDetails WHERE status = ? and
                 startTime < ?};
    return $self->{db}->doSelect($statement, $status, $timeslot);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
