#!/usr/bin/perl

# list_form.pl:  page listing reservations
# Last modified: August 16, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

require '../lib/general.pl';

my( %form_params, $tz, $starting_page );

my $cgi = CGI->new();
my $auth = Common::Auth->new();
($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                         $auth->verify_session($cgi);
if (!$form_params{user_level}) {
    print "Location:  " . $starting_page . "\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
process_form(\%form_params);
exit;
######

##############################################################################
# process_form:  Make the SOAP call, and print out the results
#
sub process_form {
    my( $form_params ) = @_;

    my( $error_status, $results );

    print $cgi->header( -type=>'text/xml' );
    $form_params->{method} = 'get_reservations';
    my $som = aaas_dispatcher($form_params);
    if ($som->faultstring) {
        update_page($som->faultstring);
        return;
    }
    $results = $som->result;
    print "<xml>\n";
    print "<msg>Successfully retrieved reservations.</msg>\n";
    print "<div id=\"zebratable_ui\">\n";
    print_reservations($results, $form_params);
    print "</div>\n";
    print "</xml>\n";
}
######

##############################################################################
# print_reservations:  print list of all reservations if the caller has admin
#                    privileges, otherwise just print that user's reservations
# In:   form parameters, results of SOAP call
# Out:  None
#
sub print_reservations {
    my ( $results, $form_params ) = @_;

    my ( $rowsref, $row );

    my $even = 0;
    $rowsref = $results->{rows};

    print "<p>Click on a column header to sort by that column. ";
    print "Times given are in the time zone of the browser. \n";
    print "Click on the Reservation Tag link to view detailed information ";
    print "about the reservation.\n";
    print "</p>\n\n";

    print "<p><form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, 'list_form', ";
    print "'$starting_page/cgi-bin/reservations/list_form.pl');\">\n";
    print '<input type="submit" value="Refresh"></input>', "\n";
    print "<input type=\"hidden\" name=\"user_dn\" value=\"$form_params->{user_dn}\"></input>\n";
    print "</form></p>\n";

    print '<table cellspacing="0" width="90%" class="sortable" ';
    print     'id="reservationlist">', "\n";
    print "<thead>\n";
    print   "<tr>\n";
    print     "<td >Tag</td>\n";
    print     "<td>Start Time</td>\n";
    print     "<td>End Time</td>\n";
    print     "<td>Status</td>\n";
    print     "<td>Origin</td>\n";
    print     "<td>Destination</td>\n";
    print   "</tr>\n";
    print "</thead>\n";

    print "<tbody>\n";
    for $row (@$rowsref) {
        if ($even) { 
            print "<tr class=\"even\">\n";
        }
        else {
            print "<tr class=\"odd\">\n";
        }
        print_row($row, $form_params->{user_level});
        print "</tr>\n";
        $even = !$even;
    }
    print "</tbody>\n";
    print "</table>\n\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n";
}
######

##############################################################################
# print_row:  print the table row corresponding to one reservation
#
# In:   returned row data, and user level
# Out:  None
#
sub print_row {
    my( $row, $user_level ) = @_;

    my( $seconds, $ip );

    print "<td>\n";
    print "<a href=\"#\" style=\"$starting_page/styleSheets/layout.css\"";
    print " onclick=\"return new_page('profile_form', ";
    print "'$starting_page/cgi-bin/reservations/details.pl?reservation_id=";
    print $row->{reservation_id}, "');\">$row->{reservation_tag}</a></td>\n";
  
    print "<td>\n";
    print   $row->{reservation_start_time};
    print "</td>\n";

    print "<td>";
    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        print $row->{reservation_end_time};
    }
    else { print "PERSISTENT\n"; }
    print "</td>\n";

    print "<td>", $row->{reservation_status}, "</td>\n";

    print "<td>", $row->{src_address}, "</td>\n";
    print "<td>", $row->{dst_address}, "</td>\n";
}
######
