#==============================================================================
package OSCARS::WBUI::BSS::ManageReservations;

=head1 NAME

OSCARS::WBUI::BSS::ManageReservations - handles request to view reservations.

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::ManageReservations;

=head1 DESCRIPTION

Makes a SOAP request to view a given set of reservations.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 24, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  Print list of all reservations if the caller has engr privileges, 
#          otherwise just print that user's reservations
# In:   results of SOAP call
# Out:  None
#
sub output_div {
    my ( $self, $results, $authorized ) = @_;

    my $msg = "Successfully retrieved reservations.";
    print( qq{
    <div>
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method="post" action="" onsubmit="return submit_form(this, 
        'server=BSS;method=ManageReservations;op=viewReservations');">
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
    for my $row (@$reservations) { $self->print_row( $row ); }
    print("</tbody></table></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# print_row:  print the table row corresponding to one reservation
#
# In:   one row of results from SOAP call
# Out:  None
#
sub print_row {
    my( $self, $row ) = @_;

    my( $end_time );

    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $row->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    print( qq{
    <tr>
      <td>
      <a href='#' style='/styleSheets/layout.css'
       onclick="return new_section(
       'server=BSS;method=ReservationDetails;reservation_id=$row->{reservation_id};');"
          >$row->{reservation_tag}</a>
      </td>
      <td>$row->{reservation_start_time}</td>
      <td>$end_time</td>
      <td>$row->{reservation_status}</td>
      <td>$row->{source_host}</td>
      <td>$row->{destination_host}</td>
    </tr>
    } );
} #____________________________________________________________________________


######
1;
