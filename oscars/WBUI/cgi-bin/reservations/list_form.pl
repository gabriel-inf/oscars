#!/usr/bin/perl

# list_form.pl:   page listing reservations
# Last modified:  November 13, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use Data::Dumper;

require '../lib/general.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{server_name} = 'BSS';
# TODO:  FIX, either user or all
$form_params->{method} = 'get_user_reservations';
my $results = get_results($form_params);
if (!$results) { exit; }

print "<xml>\n";
print "<msg>Successfully retrieved reservations.</msg>\n";
print_reservations($results, $form_params, $starting_page);
print "</xml>\n";
exit;
######

##############################################################################
# print_reservations:  print list of all reservations if the caller has admin
#                    privileges, otherwise just print that user's reservations
# In:   form parameters, results of SOAP call
# Out:  None
#
sub print_reservations {
    my ( $results, $form_params, $starting_page ) = @_;

    print qq{
    <div id="zebratable_ui">
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method="post" action="" onsubmit="return submit_form(this,
        'list_form', '$starting_page/cgi-bin/reservations/list_form.pl');">
    <input type="submit" value="Refresh"></input>
    <input type="hidden" name="user_dn" value="$form_params->{user_dn}">
    </input>
    </form></p>

    <table cellspacing="0" width="90%" class="sortable" id="reservationlist">
    <thead>
      <tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>
          <td>Origin</td><td>Destination</td>
      </tr>
    </thead>

    <tbody>
    };
    for my $row (@$results) {
        print_row($row, $starting_page);
    }
    print qq{
    </tbody>
    </table>

    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
}
######

##############################################################################
# print_row:  print the table row corresponding to one reservation
#
# In:   returned row data, and user level
# Out:  None
#
sub print_row {
    my( $row, $starting_page ) = @_;

    my( $end_time );

    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $row->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    print qq{
    <tr>
      <td>
      <a href="#" style="$starting_page/styleSheets/layout.css"
       onclick="return new_page('get_details',
       '$starting_page/cgi-bin/reservations/get_details.pl?reservation_id="
       $row->{reservation_id}');">$row->{reservation_tag}</a>
      </td>
      <td>$row->{reservation_start_time}</td>
      <td>$end_time</td>
      <td>$row->{reservation_status}</td>
      <td>$row->{source_host}</td>
      <td>$row->{destination_host}</td>
    </tr>
    };
}
######
