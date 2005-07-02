#!/usr/bin/perl

# details.pl:  Linked to by resvlist_form.pl.  Lists the details of
#              a reservation.
# Last modified: July 1, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

use BSS::Client::SOAPClient;

require '../lib/general.pl';


my( %form_params, $oscars );

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}, $oscars_home) =
                                        check_session_status(undef, $cgi);
if (!$form_params{user_dn}) {
    print "Location:  $oscars_home\n\n";
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

    my( $user_level, $error_status, $results );

    # TODO:  FIX
    $user_level = $form_params->{user_level};
    if (authorized($form_params->{user_level}, "engr")) {
        $form_params{user_level} = "engr";
    }
    else {
        $form_params{user_level} = "user";
    }
        # Check if reservation is being cancelled
    if ($form_params->{cancel}) {
        ($error_status, $results) = soap_delete_reservation($form_params);
        if ($error_status) {
            update_status_frame(1, $results->{error_msg});
            return;
        }
    }
    # print updated reservation info (may be more than just new status)
    ($error_status, $results) =
                 BSS::Client::SOAPClient::soap_get_reservations($form_params);
    if (!$error_status) {
        print_reservation_detail($user_level, $form_params, $results);
        update_status_frame(0, $results->{status_msg});
    }
    else {
        update_status_frame(1, $results->{error_msg});
    }
}
######

##############################################################################
# print_reservation_detail:  print details of reservation returned by SOAP
#                            call
# In:  user level, form parameters, and results of SOAP call 
# Out: None
#
sub print_reservation_detail {
    my ( $user_level, $form_params, $results ) = @_;

    my $row = @{$results->{rows}}[0];
    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="' . $oscars_home . 'styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript"' .
               'src="' . $oscars_home . 'main_common.js"></script>' . "\n";
    print '    <script language="javascript" type="text/javascript"' .
               'src="' . $oscars_home . 'timeprint.js"></script>' . "\n";
    print '</head>', "\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n";

    print '<script language="javascript">';
    print '    print_navigation_bar("', $user_level, '", "reservationlist");';
    print '</script>', "\n";

    print '<div id="zebratable_ui">', "\n";

    print '<p><strong>Reservation Details</strong></p>', "\n";

    print '<table cellspacing="0" width="90%" id="reservationlist">', "\n";

    print "  <tr><td>Tag</td><td>$row->{reservation_tag}</td></tr>\n"; 
    print "  <tr><td>User</td><td>$form_params->{user_dn}</td></tr>\n"; 

    print '  <tr>', "\n";
    print '  <td>Description</td>', "\n";
    print '  <td>' . $row->{reservation_description} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr><td>Start time</td><td>', "\n";
    print '    <script language="javascript">', "\n";
    print '    print_current_date("", "' .
                                 $row->{reservation_start_time} . '");' . "\n";
    print '    </script>', "\n";
    print '  </td></tr>', "\n";

    print '  <tr><td>End time</td><td>';
    if ($row->{reservation_end_time} < (2 ** 31 - 1)) {
        print '    <script language="javascript">', "\n";
        print '    print_current_date("", "' .
                                  $row->{reservation_end_time} . '");' . "\n";
        print '    </script>', "\n";
    }
    else {
        print 'PERSISTENT', "\n";
    }
    print '  </td></tr>', "\n";

    print '  <tr><td>Created time</td><td>';
    print '    <script language="javascript">', "\n";
    print '    print_current_date("", "' .  $row->{reservation_created_time} .
                                  '");' . "\n";
    print '    </script>', "\n";
    print '  </td></tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Bandwidth</td>', "\n";
    print '  <td>' . $row->{reservation_bandwidth} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Burst limit</td>', "\n";
    print '  <td>' . $row->{reservation_burst_limit} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Status</td>', "\n";
    print '  <td>' . $row->{reservation_status} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Source</td>', "\n";
    print '  <td>' . $row->{src_address} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Destination</td>', "\n";
    print '  <td>' . $row->{dst_address} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Source port</td>', "\n";
    print '  <td>' . $row->{reservation_src_port} . "</td>\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Destination port</td>', "\n";
    print '  <td>' . $row->{reservation_dst_port} . "</td>\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Protocol</td>', "\n";
    print '  <td>' . $row->{reservation_protocol} . "</td>\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>DSCP</td>', "\n";
    print '  <td>' . $row->{reservation_dscp} . "</td>\n";
    print '  </tr>', "\n";

    if ( authorized($form_params{user_level}, "engr") ) {
        print '  <tr>', "\n";
        print '  <td>Class</td>', "\n";
        print '  <td>' . $row->{reservation_class} . "</td>\n";
        print '  </tr>', "\n";

        print '  <tr>', "\n";
        print '  <td>Ingress router</td>', "\n";
        print '  <td>' . $row->{ingress_router_name} . "</td>\n";
        print '  </tr>', "\n";

        print '  <tr>', "\n";
        print '  <td>Ingress loopback</td>', "\n";
        print '  <td>' . $row->{ingress_loopback} . "</td>\n";
        print '  </tr>', "\n";

        print '  <tr>', "\n";
        print '  <td>Egress router</td>', "\n";
        print '  <td>' . $row->{egress_router_name} . "</td>\n";
        print '  </tr>', "\n";

        print '  <tr>', "\n";
        print '  <td>Egress loopback</td>', "\n";
        print '  <td>' . $row->{egress_loopback} . "</td>\n";
        print '  </tr>', "\n";

        print '  <tr>', "\n";
        print '  <td>Routers in path</td>', "\n";
        print '  <td>';
        for $_ (@{$row->{reservation_path}}) {
            if ($_ ne $row->{egress_router_name}) {
                print $_, " -> ";
            }
            else {
                print $_;
            }
        }
        print '  </td>', "\n";
        print '  </tr>', "\n";
    }

    if (($row->{reservation_status} eq 'pending') ||
        ($row->{reservation_status} eq 'active')) {
       print '<tr><td>Action: </td><td>' .
             '<a href="' . $oscars_home . 'cgi-bin/reservations/details.pl' .
             '?reservation_id=' . $row->{reservation_id} . '&cancel=1">';
       print  'CANCEL</a></td></tr>' . "\n";
    }

    print "</table>\n";
    print '<br/>';
    print '<p><form method="post" action="' . $oscars_home .
              'cgi-bin/reservations/details.pl">' . "\n";

    print '<input type="hidden" name="reservation_id" value="';
    print $form_params{reservation_id} . '">', "\n";

    print '<input type="submit" value="Refresh">', "\n";
    print '</form></p>', "\n";

    print '<a href="' . $oscars_home . 'cgi-bin/reservations/list_form.pl">';
    print '<p><strong>Back to reservations list</strong></a></p>', "\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n\n";
    print "</div>\n\n";

    print "<script language=\"javascript\">print_footer();</script>\n";
    print "</body>\n";
    print "</html>\n\n";
}
######

