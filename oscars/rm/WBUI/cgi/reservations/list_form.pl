#!/usr/bin/perl

# list_form.pl:  page listing reservations
# Last modified: June 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use BSS::Client::SOAPClient;
use Data::Dumper;

require '../lib/general.pl';


my (%form_params, %results);

my $cgi = CGI->new();
my ($dn, $user_level) = check_session_status(undef, $cgi);

if (!$error_status) {
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    $form_params{user_level} = $user_level;
    if ( (authorized($form_params{user_level}, "engr")) ||
         (authorized($form_params{user_level}, "admin"))) {
        $form_params{admin_dn} = $dn;
        $form_params{user_dn} = '*';
    }
    else { $form_params{user_dn} = $dn; }
    ($error_status, %results) = soap_get_reservations(\%form_params);
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{status_msg});
        print_reservations(\%form_params, \%results);
    }
    else {
        update_frames($error_status, "main_frame", "", $results{error_msg});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}


exit;



##### sub print_reservations
# In: 
# Out:
sub print_reservations {
    my ( $form_params, $results ) = @_;

    my ( $rowsref, $row );

    $rowsref = $results->{rows};
    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '        src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '         src="https://oscars.es.net/timeprint.js"></script>', "\n";
    print '    <script language="javascript" type="text/javascript"';
    print '         src="https://oscars.es.net/sorttable.js"></script>', "\n";
    print '</head>', "\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n";
    print '<script language="javascript">';
    print 'print_navigation_bar("', $user_level, '", "reservationlist");';
    print '</script>', "\n";

    print '<div id="zebratable_ui">', "\n\n";

    print '<p><em>View Reservations</em></p>', "\n";
    print '<p>Click on a column header to sort by that column. ';
    print 'Times given are in the time zone of the browser. ', "\n";
    print 'Click on the Reservation Tag link to view detailed information ';
    print 'about the reservation. ', "\n";
    print '</p>', "\n\n";

    print '<p><form method="post" action="https://oscars.es.net/cgi-bin/';
    print 'reservations/list_form.pl">', "\n";
    print '<input type="submit" value="Refresh">', "\n";
    print '</form></p>', "\n";

    print '<table cellspacing="0" width="90%" class="sortable" ';
    print '       id="reservationlist">', "\n";
    print '  <thead>', "\n";
    print '  <tr>', "\n";
    print '    <td >Tag</td>', "\n";
    print '    <td>Start Time</td>', "\n";
    print '    <td>End Time</td>', "\n";
    print '    <td>Status</td>', "\n";
    print '    <td>Origin</td>', "\n";
    print '    <td>Destination</td>', "\n";
    print '  </tr>', "\n";
    print '  </thead>', "\n";

    print '  <tbody>', "\n";
    for $row (@$rowsref) {
        print '  <tr>', "\n";
        print_row($row, $user_level);
        print '  </tr>', "\n";
    }
    print '  </tbody>', "\n";
    print '</table>', "\n\n";

    print '<p>For inquiries, please contact the project administrator.</p>';
    print '</div>', "\n\n";

    print '<script language="javascript">print_footer();</script>', "\n";
    print '</body>', "\n";
    print '</html>', "\n\n";
}



sub print_row
{
    my( $row, $user_level ) = @_;
    my( $seconds, $ip );

    print '    <td>', "\n";
    print '    <a href="https://oscars.es.net/cgi-bin/reservations/';
    print 'details.pl?reservation_id=' . $row->{reservation_id} . '">';
    print      $row->{reservation_tag}; 
    print '    </a></td>', "\n";
  
    print '    <td>', "\n";
    print '    <script language="javascript">';
    print '    print_current_date("", ' . $row->{reservation_start_time};
    print '                       , "local");';
    print '    </script>', "\n";
    print '    </td>', "\n";

    print '    <td>';
    print '    <script language="javascript">';
    print '    print_current_date("", ' . $row->{reservation_end_time};
    print '                      , "local");';
    print '    </script>', "\n";
    print '    </td>', "\n";

    print "    <td>" . $row->{reservation_status} . "</td>\n";

    $ip = get_oscars_host($row->{src_hostaddrs_id});
    print "    <td>" . $ip . "</td>\n";
    $ip = get_oscars_host($row->{dst_hostaddrs_id});
    print "    <td>" . $ip . "</td>\n";
}
