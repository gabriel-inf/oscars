#!/usr/bin/perl

# details.pl:  Linked to by resvlist_form.pl.  Lists the details of
#              a reservation.
# Last modified: June 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Socket;
use CGI;
use Data::Dumper;

use BSS::Client::SOAPClient;

require '../lib/general.pl';


my (%form_params, %results);

my $cgi = CGI->new();
my ($dn, $user_level) = check_session_status(undef, $cgi);

if ($dn) {
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    $form_params{user_dn} = $dn;
    $form_params{user_level} = $user_level;
        # Check if reservation is being cancelled
    if ($form_params{cancel}) {
        ($error_status, %results) = soap_delete_reservation(\%form_params);
        if (!$error_status) {
            update_frames($error_status, "main_frame", "", $results{error_msg});
            exit;
        }
    }
    # print updated reservation info (may be more than just new status)
    ($error_status, %results) =
                 BSS::Client::SOAPClient::soap_get_reservations(\%form_params);
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{status_msg});
        print_reservation_detail($user_level, \%form_params, \%results);
    }
    else {
        update_frames($error_status, "main_frame", "", $results{error_msg});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;


##### sub print_reservation_detail
# In: 
# Out:
sub print_reservation_detail
{
    my ( $user_level, $form_params, $results ) = @_;

    my $row = @{$results->{rows}}[0];
    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '        src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '        src="https://oscars.es.net/timeprint.js"></script>', "\n";
    print '</head>', "\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n";

    print '<script language="javascript">';
    print '    print_navigation_bar("', $user_level, '", "reservationlist");';
    print '</script>', "\n";

    print '<div id="zebratable_ui">', "\n\n";

    print '<p><form method="post" action="https://oscars.es.net/cgi-bin/';
    print 'reservations/details.pl">', "\n";

    print '<input type="hidden" name="reservation_id" value="';
    print $form_params{reservation_id} . '">', "\n";

    print '<input type="submit" value="Refresh">', "\n";
    print '</form></p>', "\n";

    print '<table cellspacing="0" width="90%" id="reservationlist">', "\n";

    print "  <tr><td>Tag:  </td><td>$row->{reservation_tag}</td></tr>\n"; 

    print '  <tr><td>Start Time:  </td><td>', "\n";
    print '    <script language="javascript">', "\n";
    print '    print_current_date("", ' . $row->{reservation_start_time};
    print '                       , "local");', "\n";
    print '    </script>', "\n";
    print '  </td></tr>', "\n";

    print '  <tr><td>End Time:  </td><td>';
    print '    <script language="javascript">', "\n";
    print '    print_current_date("", ' . $row->{reservation_end_time};
    print '                       , "local");', "\n";
    print '    </script>', "\n";
    print '  </td></tr>', "\n";

    print '  <tr><td>Created Time:  </td><td>';
    print '    <script language="javascript">', "\n";
    print '    print_current_date("", ' . $row->{reservation_created_time};
    print '                       , "local");', "\n";
    print '    </script>', "\n";
    print '  </td></tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Bandwidth:  </td>', "\n";
    print '  <td>' . $row->{reservation_bandwidth} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Burst Limit:  </td>', "\n";
    print '  <td>' . $row->{reservation_burst_limit} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Status:  </td>', "\n";
    print '  <td>' . $row->{reservation_status} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Origin:  </td>', "\n";
    print '  <td>' . get_oscars_host($row->{src_hostaddrs_id}) . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Destination:  </td>', "\n";
    print '  <td>' . get_oscars_host($row->{dst_hostaddrs_id}) . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr>', "\n";
    print '  <td>Description:  </td>', "\n";
    print '  <td>' . $row->{reservation_description} . '</td>', "\n";
    print '  </tr>', "\n";

    print '  <tr><td>Action: </td><td>';
    if (($row->{reservation_status} eq 'pending') ||
        ($row->{reservation_status} eq 'active')) {
       print  '<a href="https://oscars.es.net/cgi-bin/reservations/details.pl';
       print  '?reservation_id=' . $row->{reservation_id} . '&cancel=1">';
       print  'CANCEL</a></td></tr>' . "\n";
    }
    else { print '<td></td></tr>', "\n"; }

    print "</table>\n";
    print '<br/><br/>';
    print '<a href="https://oscars.es.net/cgi-bin/reservations/list_form.pl">';
    print 'Back to reservations list</a>', "\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n\n";
    print "</div>\n\n";

    print "<script language=\"javascript\">print_footer();</script>\n";
    print "</body>\n";
    print "</html>\n\n";
}

