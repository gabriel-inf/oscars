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

April 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  Print list of all reservations if the caller is authorized, 
#             otherwise just print that user's reservations
# In:   results of SOAP call
# Out:  None
#
sub outputDiv {
    my ( $self, $results, $authorizations ) = @_;

    my $msg = "Successfully retrieved reservations.";
    print( qq{
    <div>
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method="post" action="" onsubmit="return submit_form(this, 
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
    my $reservations = $results->{list};
    for my $row (@$reservations) { $self->printRow( $row ); }
    print("</tbody></table></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# printRow:  print the table row corresponding to one reservation
#
# In:   one row of results from SOAP call
# Out:  None
#
sub printRow {
    my( $self, $row ) = @_;

    my( $endTime );

    if ($row->{endTime} ne '2039-01-01 00:00:00') {
        $endTime = $row->{endTime};
    }
    else { $endTime = 'PERSISTENT'; }
    print( qq{
    <tr>
      <td>
      <a href='#' style='/styleSheets/layout.css'
       onclick="return new_section(
       'method=QueryReservation;id=$row->{id};');" >$row->{tag}</a>
      </td>
      <td>$row->{startTime}</td>
      <td>$endTime</td>
      <td>$row->{status}</td>
      <td>$row->{srcHost}</td>
      <td>$row->{destHost}</td>
    </tr>
    } );
} #____________________________________________________________________________


######
1;
