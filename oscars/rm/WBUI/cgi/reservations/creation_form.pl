#!/usr/bin/perl

# creation_form.pl:  form for making reservations
# Last modified: August 24, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

use Common::Auth;
use AAAS::Client::SOAPClient;

require '../lib/general.pl';

my( %form_params, $starting_page, $tz );

my $cgi = CGI->new();
my $auth = Common::Auth->new();
($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                         $auth->verify_session($cgi);
print $cgi->header( -type=>'text/xml' );
if (!$form_params{user_level}) {
    print "Location:  " . $starting_page . "\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"reservation_ui\">\n";
print_reservation_form(\%form_params, $tz);
print  "</div>\n";
print  "</xml>\n";
exit;

######

###############################################################################
# print_reservation_form:  prints out the reservation creation form
#                         accessible from the "Make a Reservation" notebook tab
# In:   user level
# Out:  none
#
sub print_reservation_form {
    my( $form_params, $tz ) = @_;

    print "<form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, 'details', ";
    print "'$starting_page/cgi-bin/reservations/details.pl');\">";

    print '<input type="hidden" name="create" value="1"></input>', "\n";
    print '<input type="hidden" name="reservation_start_time"></input>', "\n";
    print '<input type="hidden" name="reservation_end_time"></input>', "\n";
    print "<input type=\"hidden\" name=\"user_dn\" value=\"$form_params->{user_dn}\"></input>\n";

    print "<p>Required inputs are bordered in green. ",
          "Ranges or types of valid entries are given in parentheses below the ",
          " input fields.</p>\n";

    print "<table>\n";
    print   "<tr>";
    print     "<th>Source</th>";
    print     "<th>Destination</th>";
    print     "<th>Bandwidth (Mbps)</th>";
    print   "</tr>\n";
    print   "<tr>";
    print     '<td bgcolor="00cc44"><input type="text" name="src_address" size="30"></input></td>';
    print     '<td bgcolor="00cc44"><input type="text" name="dst_address" size="30"></input></td>';
    print     '<td bgcolor="00cc44"><input type="text" name="reservation_bandwidth" size="18"></input></td>';
    print   "</tr>\n";
    print   "<tr>";
    print     "<td>(Host name or IP address)</td>";
    print     "<td>(Host name or IP address)</td>";
    print     "<td>(10-10000)</td>";
    print   "</tr>\n";
    print "</table>\n";

    print "<table cols=\"4\">";
    print   "<tr>";
    print     "<td colspan=\"4\">";
    print     "<strong>DSCP</strong> sets the differentiated services ";
    print     "code point.";
    print     "</td>";
    print   "</tr>\n";
    print   "<tr>";
    print     "<td colspan=\"4\"> </td>";
    print   "</tr>\n";
    print   "<tr>";
    print     "<th>Source port</th>";
    print     "<th>Destination port</th>";
    print     "<th>Protocol</th>";
    print     "<th>DSCP</th>";
    print   "</tr>\n";
    print   "  <tr>";
    print     "<td><input type=\"text\" name=\"reservation_src_port\"";
    print           " maxlength=\"5\"></input></td>";
    print     "<td><input type=\"text\" name=\"reservation_dst_port\" maxlength=\"5\"></input></td>";
    print     "<td><input type=\"text\" name=\"reservation_protocol\"></input></td>";
    print     "<td><input type=\"text\" name=\"reservation_dscp\"";
    print           " maxlength=\"2\"></input></td>";
    print    "</tr>\n";
    print    "<tr>";
    print      "<td>(1024-65535)</td>";
    print      "<td>(1024-65535)</td>";
    print      "<td>(0-255), or string</td>";
    print      "<td>(0-63)</td>";
    print    "</tr>\n";
    print "</table>\n";

    if ($auth->authorized($form_params->{user_level}, "engr")) {
        print '<p><strong>WARNING</strong>:  Entries in the following ';
        print 'fields may change default routing behavior for the selected ';
        print 'flow.</p>', "\n";

        print '<table cols="2">', "\n";
        print   "<tr>";
        print     '<td colspan="2"> </td>';
        print   "</tr>\n";
        print   "<tr>";
        print     "<th>Ingress loopback</th>";
        print     "<th>Egress loopback</th>";
        print   "</tr>\n";
        print   "<tr>";
        print     '<td><input type="text" name="lsp_from"></input></td>';
        print     '<td><input type="text" name="lsp_to"></input></td>';
        print   "</tr>\n";
        print   "<tr>";
        print     "<td>(IP address)</td>";
        print     "<td>(IP address)</td>";
        print   "</tr>\n";
        print "</table>\n";
    }

    print "<p>Indicate the starting date and time, and the duration in hours, ";
    print "of your reservation. ";
    print "Fields left blank will default to the examples ";
    print "below the input fields.  The default time zone is the local time.  ";
    if ($auth->authorized($form_params->{user_level}, "engr")) {
        print "Checking the <strong>Persistent</strong> box makes ";
        print "a reservation's duration indefinite, overriding ";
        print "the duration field.";
    }
    print "</p>\n";

    print "<table>\n";
    print   "<tr>\n";
    print     "<th>Year</th>";
    print     "<th>Month</th>";
    print     "<th>Date</th>";
    print     "<th>Hour</th>";
    print     "<th>Minute</th>";
    print     "<th>UTC offset</th>";
    print     "<th>Duration (Hours)</th>";
    if ($auth->authorized($form_params->{user_level}, "engr")) {
        print "<th>Persistent</th>";
    }
    else { print "<th> </th>"; }
    print   "</tr>\n";
    print   '<tr class="alignright">', "\n";
    print     "<td>";
    print       '<input type="text" name="start_year" size="4" maxlength="4">';
    print     "</input></td>";
    print     "<td>";
    print       '<input type="text" name="start_month" size="4" maxlength="2">';
    print     "</input></td>";
    print     "<td>";
    print       '<input type="text" name="start_date" size="4" maxlength="2">';
    print       "</input></td>";
    print     "<td>";
    print       '<input type="text" name="start_hour" size="4" maxlength="2">';
    print     "</input></td>";
    print     "<td>";
    print       '<input type="text" name="start_minute" size="4" maxlength="2">';
    print     "</input></td>";
    print     "<td id=\"get_timezone_options\"> </td>\n";
    print     "<td>\n";
    print       '<input type="text" name="duration_hour" size="16" ',
                     'maxlength="16">';
    print     "</input></td>";
    print     "<td> ";
    if ($auth->authorized($form_params->{user_level}, "engr")) {
        print '  <input type="checkbox" name="persistent" value="0">';
    }
    print     "</input></td>\n";
    print   "</tr>\n";
    print   "<tr class=\"alignright\" id=\"get_time_settings_example\">\n";
    print     "<td colspan=\"8\"> </td>";
    print   "</tr>\n";
    print   "<tr>\n";
    print     "<td> </td>\n";
    print     "<td>(1-12)</td>\n";
    print     "<td>(1-31)</td>\n";
    print     "<td>(0-23)</td>\n";
    print     "<td>(0-59)</td>\n";
    print     "<td> </td>\n";
    print     "<td>(0.01-INF)</td>\n";
    print     "<td> </td>\n";
    print   "</tr>\n";
    print "</table>\n";

    print '<table cols="1">', "\n";
    print   "<tr>\n";
    print     "<td>Please let us know the purpose of making this reservation.</td>\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print   "<td></td>\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print     '<td bgcolor="00cc44"><textarea name="reservation_description" rows="2" cols="72"> </textarea></td>', "\n";

    print   "</tr>\n";
    print "</table>\n";

    print '<input type="submit" value="Reserve bandwidth"></input>', "\n";
    print '<input type="reset" value="Reset form fields"></input>', "\n";

    print "</form>\n";
}
######
