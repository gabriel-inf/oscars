#!/usr/bin/perl

# print_details.pl:  Prints the details of a reservation.
# Last modified:     November 13, 2005
# David Robertson    (dwrobertson@lbl.gov)
# Soo-yeon Hwang     (dapi@umich.edu)

require '../lib/general.pl';

##############################################################################
# print_reservation_detail:  print details of reservation returned by SOAP
#                            call
# In:  user level, form parameters, and results of SOAP call 
# Out: None
#
sub print_reservation_detail {
    my( $form_params, $results, $msg, $starting_page ) = @_;

    my $ctr = 0;

    print "<xml>";
    print "<msg>$msg</msg>";
    print '<div id="zebratable_ui">', "\n";

    print "<p><strong>Reservation Details</strong></p>\n";

    print '<table width="90%" id="reservationlist">', "\n";

    $ctr = start_row($ctr);
    print     "<td>Tag</td><td>$results->{reservation_tag}</td></tr>\n"; 
    $ctr = start_row($ctr);
    print     "<td>User</td><td>$form_params->{user_dn}</td></tr>\n"; 

    $ctr = start_row($ctr);
    print   "<td>Description</td>";
    print   "<td>", $results->{reservation_description}, "</td>";
    print "</tr>\n";

    $ctr = start_row($ctr);
    print  "<td>Start time</td><td>";
    print     "$results->{reservation_start_time}";
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print  "<td>End time</td><td>";
    if ($results->{reservation_end_time}) {
        print "$results->{reservation_end_time}";
    }
    else { print 'PERSISTENT'; }
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print  "<td>Created time</td><td>";
    print   "$results->{reservation_created_time}";
    print   "</td></tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Bandwidth</td>";
    print     "<td>", $results->{reservation_bandwidth}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Burst limit</td>";
    print     "<td>", $results->{reservation_burst_limit}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Status</td>";
    print     "<td>", $results->{reservation_status}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Source</td>";
    print     "<td>", $results->{source_host}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Destination</td>";
    print     "<td>", $results->{destination_host}, "</td>";
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Source port</td>";
    if ($results->{reservation_src_port}) {
        print "<td>", $results->{reservation_src_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Destination port</td>";
    if ($results->{reservation_dst_port}) {
        print "<td>", $results->{reservation_dst_port}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>Protocol</td>";
    if ($results->{reservation_protocol}) {
        print "<td>", $results->{reservation_protocol}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    $ctr = start_row($ctr);
    print     "<td>DSCP</td>";
    if ($results->{reservation_dscp}) {
        print "<td>", $results->{reservation_dscp}, "</td>";
    }
    else { print "<td>DEFAULT</td>"; }
    print   "</tr>\n";

    # TODO:  AAAS must undef these if user doesn't have authorization to set
    if ( $results->{reservaton_class} ) {
        $ctr = start_row($ctr);
        print   "<td>Class</td>";
        print   "<td>", $results->{reservation_class}, "</td>";
        print "</tr>\n";
    }
    if ( $results->{ingress_router} ) {
        $ctr = start_row($ctr);
        print   "<td>Ingress router</td>";
        print   "<td>", $results->{ingress_router}, "</td>";
        print "</tr>\n";
    }
    if ( $results->{ingress_ip} ) {
        $ctr = start_row($ctr);
        print   "<td>Ingress loopback</td>";
        print   "<td>", $results->{ingress_ip}, "</td>";
        print "</tr>\n";
    }
    if ( $results->{egress_router} ) {
        $ctr = start_row($ctr);
        print   "<td>Egress router</td>";
        print   "<td>", $results->{egress_router}, "</td>";
        print "</tr>\n";
    }
    if ( $results->{egress_ip} ) {
        $ctr = start_row($ctr);
        print   "<td>Egress loopback</td>";
        print   "<td>", $results->{egress_ip}, "</td>";
        print "</tr>\n";
    }
    if ( $results->{reservation_path} ) {
        $ctr = start_row($ctr);
        print   "<td>Routers in path</td>";
        print   "<td>";
        my $path_str = "";
        for $_ (@{$results->{reservation_path}}) {
            $path_str .= $_ . " -> ";
        }
        # remove last '->'
        substr($path_str, -4, 4) = '';
        print $path_str;
        print   "</td>";
        print "</tr>\n";
    }

    if (($results->{reservation_status} eq 'pending') ||
        ($results->{reservation_status} eq 'active')) {
        $ctr = start_row($ctr);
        print "<td>Action: </td>";
        print "<td>\n";
        print "<a href=\"#\"";
        print " style=\"$starting_page/styleSheets/layout.css\"\n";
        print " onclick=\"return new_page('details', ";
        print "'$starting_page/cgi-bin/reservations/cancel.pl?reservation_id=$results->{reservation_id}');\"\n";
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
        ");\">$results->{user_last_name}\n";
    print "<strong>Back to reservations list</strong></a></p>\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n\n";
    print  "</div>\n";
    print  "</xml>\n";
}
######

######
1;
