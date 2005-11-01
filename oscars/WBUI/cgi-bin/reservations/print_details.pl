#!/usr/bin/perl

# print_details.pl:  Prints the details of a reservation.
# Last modified: October 31, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

require '../lib/general.pl';

##############################################################################
# print_reservation_detail:  print details of reservation returned by SOAP
#                            call
# In:  user level, form parameters, and results of SOAP call 
# Out: None
#
sub print_reservation_detail {
    my( $form_params, $results, $msg, $starting_page ) = @_;

    my $row = @{$results->{rows}}[0];
    my $ctr = 0;

    print "<xml>";
    print "<msg>$msg</msg>";
    print '<div id="zebratable_ui">', "\n";

    print "<p><strong>Reservation Details</strong></p>\n";

    print '<table width="90%" id="reservationlist">', "\n";

    $ctr = start_row($ctr);
    print     "<td>Tag</td><td>$row->{reservation_tag}</td></tr>\n"; 
    $ctr = start_row($ctr);
    print     "<td>User</td><td>$form_params->{user_dn}</td></tr>\n"; 

    $ctr = start_row($ctr);
    print   "<td>Description</td>";
    print   "<td>", $row->{reservation_description}, "</td>";
    print "</tr>\n";

    $ctr = start_row($ctr);
    print  "<td>Start time</td><td>";
    print     "$row->{reservation_start_time}";
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print  "<td>End time</td><td>";
    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        print "$row->{reservation_end_time}";
    }
    else { print "PERSISTENT"; }
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print  "<td>Created time</td><td>";
    print   "$row->{reservation_created_time}";
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Bandwidth</td>";
    print     "<td>", $row->{reservation_bandwidth}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Burst limit</td>";
    print     "<td>", $row->{reservation_burst_limit}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Status</td>";
    print     "<td>", $row->{reservation_status}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Source</td>";
    print     "<td>", $row->{source_host}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Destination</td>";
    print     "<td>", $row->{destination_host}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Source port</td>";
    if ($row->{reservation_src_port}) {
        print "<td>", $row->{reservation_src_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Destination port</td>";
    if ($row->{reservation_dst_port}) {
        print "<td>", $row->{reservation_dst_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Protocol</td>";
    if ($row->{reservation_protocol}) {
        print "<td>", $row->{reservation_protocol}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>DSCP</td>";
    if ($row->{reservation_dscp}) {
        print "<td>", $row->{reservation_dscp}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    if ( authorized($form_params->{user_level}, "engr") ) {
        $ctr = start_row($ctr);
        print   "<td>Class</td>";
        print   "<td>", $row->{reservation_class}, "</td>";
        print "</tr>\n";

        $ctr = start_row($ctr);
        print   "<td>Ingress router</td>";
        print   "<td>", $row->{ingress_router}, "</td>";
        print "</tr>\n";

        $ctr = start_row($ctr);
        print   "<td>Ingress loopback</td>";
        print   "<td>", $row->{ingress_ip}, "</td>";
        print "</tr>\n";

        $ctr = start_row($ctr);
        print   "<td>Egress router</td>";
        print   "<td>", $row->{egress_router}, "</td>";
        print "</tr>\n";

        $ctr = start_row($ctr);
        print   "<td>Egress loopback</td>";
        print   "<td>", $row->{egress_ip}, "</td>";
        print "</tr>\n";

        $ctr = start_row($ctr);
        print   "<td>Routers in path</td>";
        print   "<td>";
        my $path_str = "";
        for $_ (@{$row->{reservation_path}}) {
            $path_str .= $_ . " -> ";
        }
        # remove last '->'
        substr($path_str, -4, 4) = '';
        print $path_str;
        print   "</td>";
        print "</tr>\n";
    }

    if (($row->{reservation_status} eq 'pending') ||
        ($row->{reservation_status} eq 'active')) {
        $ctr = start_row($ctr);
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
