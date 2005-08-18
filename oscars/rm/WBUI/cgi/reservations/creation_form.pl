#!/usr/bin/perl

# creation_form.pl:  form for making reservations
# Last modified: August 17, 2005
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
print_reservation_form($form_params{user_level});
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
    my( $user_level ) = @_;

    print "<form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, 'details', ";
    print "'$starting_page/cgi-bin/test/reservations/details.pl');\">";

    print '<input type="hidden" name="create" value="1"></input>', "\n";
    print '<input type="hidden" name="reservation_start_time"></input>', "\n";
    print '<input type="hidden" name="reservation_end_time"></input>', "\n";

    print "<p>Required inputs are bordered in green. ",
          "Ranges or types of valid entries are given in parentheses below the ",
          " input fields.</p>\n";

    print "<table>\n";
    print   "<tr>\n";
    print     "<th>Source</th>\n";
    print     "<th>Destination</th>\n";
    print     "<th>Bandwidth (Mbps)</th>\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print     '<td bgcolor="00cc44"><input type="text" name="src_address" size="30"></input></td>', "\n";
    print     '<td bgcolor="00cc44"><input type="text" name="dst_address" size="30"></input></td>', "\n";
    print     '<td bgcolor="00cc44"><input type="text" name="reservation_bandwidth" size="18"></input></td>', "\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print     "<td>(Host name or IP address)</td>\n";
    print     "<td>(Host name or IP address)</td>\n";
    print     "<td>(10-10000)</td>\n";
    print   "</tr>\n";
    print "</table>\n";

    print '<table cols="4">', "\n";
    print   "<tr>\n";
    print     '<td colspan="4">', "\n";
    print     "<strong>DSCP</strong> sets the differentiated services ";
    print     "code point.\n";
    print     "</td>\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print     '<td colspan="4"> </td>', "\n";
    print   "</tr>\n";
    print   "<tr>\n";
    print     "<th>Source port</th>\n";
    print     "<th>Destination port</th>\n";
    print     "<th>Protocol</th>\n";
    print     "<th>DSCP</th>\n";
    print   "</tr>\n";
    print   "  <tr>\n";
    print     '<td><input type="text" name="reservation_src_port" ',
                   'maxlength="5"></input></td>', "\n";
    print     '<td><input type="text" name="reservation_dst_port" ',
                   'maxlength="5"></input></td>', "\n",
    print     '<td><input type="text" name="reservation_protocol" ',
                   '></input></td>', "\n";
    print     '<td><input type="text" name="reservation_dscp" ',
                   'maxlength="2"></input></td>', "\n";
    print    "</tr>\n";
    print    "<tr>\n";
    print      "<td>(1024-65535)</td>\n";
    print      "<td>(1024-65535)</td>\n";
    print      "<td>(0-255), or string</td>\n";
    print      "<td>(0-63)</td>\n";
    print    "</tr>\n";
    print "</table>\n";

    if ($auth->authorized($user_level, "engr")) {
        print '<p><strong>WARNING</strong>:  Entries in the following ';
        print 'fields may change default routing behavior for the selected ';
        print 'flow.</p>', "\n";

        print '<table cols="2">', "\n";
        print   "<tr>\n";
        print     '<td colspan="2"> </td>', "\n";
        print   "</tr>\n";
        print   "<tr>\n";
        print     "<th>Ingress loopback</th>\n";
        print     "<th>Egress loopback</th>\n";
        print   "</tr>\n";
        print   "<tr>\n";
        print     '<td><input type="text" name="lsp_from"></input></td>', "\n";
        print     '<td><input type="text" name="lsp_to"></input></td>', "\n";
        print   "</tr>\n";
        print   "<tr>\n";
        print     "<td>(IP address)</td>\n";
        print     "<td>(IP address)</td>\n";
        print   "</tr>\n";
        print "</table>\n";
    }

    print "<p>Indicate the starting date and time, and the duration in hours, ";
    print "of your reservation. ";
    print "Fields left blank will default to the examples ";
    print "below the input fields.  The default time zone is the local time.  ";
    if ($auth->authorized($user_level, "engr")) {
        print "Checking the <strong>Persistent</strong> box makes ";
        print "a reservation's duration indefinite, overriding ";
        print "the duration field.</p>\n";
    }
    else {
        print "</p>\n";
    }
    print "<table>\n";
    print   "<tr>\n";
    print     "<th>Year</th>\n";
    print     "<th>Month</th>\n";
    print     "<th>Date</th>\n";
    print     "<th>Hour</th>\n";
    print     "<th>Minute</th>\n";
    print     "<th>UTC offset</th>\n";
    print     "<th>Duration (Hours)</th>\n";
    if ($auth->authorized($user_level, "engr")) {
        print "<th>Persistent</th>\n";
    }
    else { print "<th> </th>\n"; }
    print   "</tr>\n";
    print   '<tr class="alignright">', "\n";
    print     "<td>";
    print       '<input type="text" name="start_year" size="4" maxlength="4">';
    print     "</input></td>\n";
    print     "<td>";
    print       '<input type="text" name="start_month" size="4" maxlength="2">';
    print     "</input></td>\n";
    print     "<td>";
    print       '<input type="text" name="start_date" size="4" maxlength="2">';
    print       "</input></td>\n";
    print     "<td>";
    print       '<input type="text" name="start_hour" size="4" maxlength="2">';
    print     "</input></td>\n";
    print     "<td>";
    print       '<input type="text" name="start_minute" size="4" maxlength="2">';
    print     "</input></td>\n";
    print     "<td>";
    print       '<select name="reservation_time_zone">', "\n";
    print       '<script language="javascript">print_timezone_options();</script>', "\n";
    print       "</select>";
    print     "</td>\n";
    print     "<td>\n";
    print       '<input type="text" name="duration_hour" size="16" ',
                     'maxlength="6">', "\n";
    print     "</input></td>\n";
    print     "<td> \n";
    if ($auth->authorized($user_level, "engr")) {
        print '  <input type="checkbox" name="persistent" value="0">';
    }
    print     "</input></td>\n";
    print   "</tr>\n";
    print   '<tr class="alignright">', "\n";
    print     '<script language="javascript">print_time_settings_example();</script>', "\n";
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
