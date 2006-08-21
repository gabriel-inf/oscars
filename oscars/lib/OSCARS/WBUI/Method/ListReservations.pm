#==============================================================================
package OSCARS::WBUI::Method::ListReservations;

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

OSCARS::WBUI::Method::ListReservations - handles request to view reservations.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ListReservations;

=head1 DESCRIPTION

Makes a SOAP request to view a given set of reservations.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'ListReservations';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  Print list of all reservations returned from SOAP server. 
# In:   response from SOAP call
# Out:  None
#
sub outputContent {
    my ( $self, $request, $response ) = @_;

    my $timeHandler = OSCARS::WBUI::Method::ReservationDetails->new();
    my $msg = "Successfully retrieved reservations.";
    print( qq{
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method="post" action="" onsubmit="return submitForm(this, 
        'method=ListReservations;');">
    <input type='submit' value='Refresh'></input>
    </form></p>

    <table cellspacing='0' width='90%' class='sortable'>
    <thead>
      <tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>
          <td>Origin</td><td>Destination</td>
      </tr>
    </thead>
    <tbody>
    } );
    my $reservations = $response;
    for my $row (@$reservations) { $self->printRow( $row, $timeHandler ); }
    print("</tbody></table>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# printRow:  print the table row corresponding to one reservation
#
# In:   one row of response from SOAP call
# Out:  None
#
sub printRow {
    my( $self, $row, $timeHandler ) = @_;

    my $startTime = $timeHandler->formatTime($row->{startTime});
    my $endTime = $timeHandler->formatTime($row->{endTime});
    print( qq{
    <tr>
      <td>
      <a href='#' style='/styleSheets/layout.css'
       onclick="return newSection(
       'method=QueryReservation;tag=$row->{tag};');" >$row->{tag}</a>
      </td>
      <td>$startTime</td>
      <td>$endTime</td>
      <td>$row->{status}</td>
      <td>$row->{srcHost}</td>
      <td>$row->{destHost}</td>
    </tr>
    } );
} #____________________________________________________________________________


######
1;
