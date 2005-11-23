##############################################################################
package Client::BSS::ViewReservations;

# Handles request to view a given set of reservations.
#
# Last modified:  November 22, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#_____________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'BSS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  print list of all reservations if the caller has 
#          engr privileges, otherwise just print that user's reservations
# In:   results of SOAP call
# Out:  None
#
sub output {
    my ( $self, $results ) = @_;

    my $params_str;

    print $self->{cgi}->header(
         -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>Successfully retrieved reservations.</msg>
    <div id='zebratable_ui'>
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method='post' action='' onsubmit="return submit_form(this,
            'view_reservations', '');">
    <input type='submit' value='Refresh'></input>
    </input>
    </form></p>

    <table cellspacing='0' width='90%' class='sortable' id='reservationlist'>
    <thead>
      <tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>
          <td>Origin</td><td>Destination</td>
      </tr>
    </thead>

    <tbody>
    };
    for my $row (@$results) { $self->print_row( $row ); }
    print qq{
    </tbody>
    </table>

    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print "</xml>\n";
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
    print qq{
    <tr>
      <td>
      <a href='#' style='/styleSheets/layout.css'
       onclick="return new_section(
          'view_details', 'reservation_id=$row->{reservation_id}');"
          >$row->{reservation_tag}</a>
      </td>
      <td>$row->{reservation_start_time}</td>
      <td>$end_time</td>
      <td>$row->{reservation_status}</td>
      <td>$row->{source_host}</td>
      <td>$row->{destination_host}</td>
    </tr>
    };
} #____________________________________________________________________________


######
1;
