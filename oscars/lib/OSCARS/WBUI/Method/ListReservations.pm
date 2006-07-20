#==============================================================================
package OSCARS::WBUI::Method::ListReservations;

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
