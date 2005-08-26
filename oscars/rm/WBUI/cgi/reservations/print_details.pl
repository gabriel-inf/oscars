#!/usr/bin/perl

# details.pl:  Prints the details of a reservation.
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)


##############################################################################
# print_reservation_detail:  print details of reservation returned by SOAP
#                            call
# In:  user level, form parameters, and results of SOAP call 
# Out: None
#
sub print_reservation_detail {
    my( $form_params, $results, $msg, $auth, $starting_page ) = @_;

    my $row = @{$results->{rows}}[0];
    my $even = 0;

    print "<xml>";
    print "<msg>$msg</msg>";
    print '<div id="zebratable_ui">', "\n";

    print "<p><strong>Reservation Details</strong></p>\n";

    print '<table cellspacing="0" width="90%" id="reservationlist">', "\n";

    print   "<tr class=\"odd\">";
    $even = !$even;
    print     "<td>Tag</td><td>$row->{reservation_tag}</td></tr>\n"; 
    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>User</td><td>$form_params->{user_dn}</td></tr>\n"; 

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print   "<td>Description</td>";
    print   "<td>", $row->{reservation_description}, "</td>";
    print "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print  "<td>Start time</td><td>";
    print     "$row->{reservation_start_time}";
    print   "</td></tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print  "<td>End time</td><td>";
    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        print "$row->{reservation_end_time}";
    }
    else { print "PERSISTENT"; }
    print   "</td></tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print  "<td>Created time</td><td>";
    print   "$row->{reservation_created_time}";
    print   "</td></tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Bandwidth</td>";
    print     "<td>", $row->{reservation_bandwidth}, "</td>";
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    print     "<td>Burst limit</td>";
    print     "<td>", $row->{reservation_burst_limit}, "</td>";
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Status</td>";
    print     "<td>", $row->{reservation_status}, "</td>";
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Source</td>";
    print     "<td>", $row->{src_address}, "</td>";
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Destination</td>";
    print     "<td>", $row->{dst_address}, "</td>";
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Source port</td>";
    if ($row->{reservation_src_port}) {
        print "<td>", $row->{reservation_src_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Destination port</td>";
    if ($row->{reservation_dst_port}) {
        print "<td>", $row->{reservation_dst_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>Protocol</td>";
    if ($row->{reservation_protocol}) {
        print "<td>", $row->{reservation_protocol}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    if ($even) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    $even = !$even;
    print     "<td>DSCP</td>";
    if ($row->{reservation_dscp}) {
        print "<td>", $row->{reservation_dscp}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    if ( $auth->authorized($form_params->{user_level}, "engr") ) {
        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Class</td>";
        print   "<td>", $row->{reservation_class}, "</td>";
        print "</tr>\n";

        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Ingress router</td>";
        print   "<td>", $row->{ingress_router_name}, "</td>";
        print "</tr>\n";

        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Ingress loopback</td>";
        print   "<td>", $row->{ingress_loopback}, "</td>";
        print "</tr>\n";

        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Egress router</td>";
        print   "<td>", $row->{egress_router_name}, "</td>";
        print "</tr>\n";

        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Egress loopback</td>";
        print   "<td>", $row->{egress_loopback}, "</td>";
        print "</tr>\n";

        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print   "<td>Routers in path</td>";
        print   "<td>";
        for $_ (@{$row->{reservation_path}}) {
            if ($_ ne $row->{egress_router_name}) {
                #print $_, " -> ";
                print $_;
            }
            else { print $_; }
        }
        print   "</td>";
        print "</tr>\n";
    }

    if (($row->{reservation_status} eq 'pending') ||
        ($row->{reservation_status} eq 'active')) {
        if ($even) { print "<tr class=\"even\">"; }
        else { print "<tr class=\"odd\">"; }
        $even = !$even;
        print "<td>Action: </td>";
        print "<td>\n";
        print "<a href=\"#\"";
        print " style=\"$starting_page/styleSheets/layout.css\"\n";
        print " onclick=\"return new_page('details', ";
        print "'$starting_page/cgi-bin/reservations/cancel.pl?reservation_id=$row->{reservation_id}');\"\n";
        print ">CANCEL</a>\n";
        print "</td>";
        print "</tr>\n";
    }

    print "</table>\n";
    print "<form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, ";
    print "'details', '$starting_page/cgi-bin/reservations/get_details.pl');\">\n";

    print "<input type=\"hidden\" name=\"user_dn\" value=\"$form_params->{user_dn}\"></input>\n";
    print "<input type=\"hidden\" name=\"reservation_id\" value=";
    print "\"$form_params->{reservation_id}\"></input>\n";

    print "<p><input type=\"submit\" value=\"Refresh\"></input></p>\n";
    print "</form>\n";

    print "<p><a href=\"#\" style=\"$starting_page/cgi-bin/reservations/list_form.pl\"";
    print " onclick=\"return new_page";
    print "('list_form', '$starting_page/cgi-bin/reservations/list_form.pl'",
        ");\">$row->{user_last_name}\n";
    print "<strong>Back to reservations list</strong></a></p>\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n\n";
    print  "</div>\n";
    print  "</xml>\n";
}
######

######
1;
