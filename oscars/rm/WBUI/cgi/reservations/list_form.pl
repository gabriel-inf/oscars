#!/usr/bin/perl

# list_form.pl:  page listing reservations
# Last modified: June 17, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;

use BSS::Client::SOAPClient;
use Data::Dumper;

require '../lib/general.pl';


my (%form_params);
my $cgi = CGI->new();

($form_params{user_dn}, $form_params{$user_level}) =
                                       check_session_status(undef, $cgi);
if (!$form_params{user_dn}) {
    print "Location:  https://oscars.es.net/\n\n";
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

    # Get all fields if user has engineer's privileges
    if ( authorized($form_params->{user_level}, "engr") ) {
        $form_params->{user_level} = 'engr';
    }
    else { $form_params->{user_dn} = $dn; }
    ($error_status, $results) = soap_get_reservations($form_params);
    if (!$error_status) {
        update_frames($error_status, "success", "main_frame", "", $results->{status_msg});
        print_reservations($form_params, $results);
    }
    else {
        update_frames($error_status, "error", "main_frame", "", $results->{error_msg});
    }
}
######

##############################################################################
# print_reservations:  print list of all reservations if the caller has admin
#                    privileges, otherwise just print that user's reservations
# In:   form parameters, results of SOAP call
# Out:  None
#
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
    if ($row->{reservation_end_time} < (2 ** 31 - 1)) {
        print '    <script language="javascript">';
        print '    print_current_date("", ' . $row->{reservation_end_time};
        print '                      , "local");';
        print '    </script>', "\n";
    }
    else {
        print 'PERSISTENT', "\n";
    }
    print '    </td>', "\n";

    print "    <td>" . $row->{reservation_status} . "</td>\n";

    $ip = get_oscars_host($row->{src_host_ip});
    print "    <td>" . $ip . "</td>\n";
    $ip = get_oscars_host($row->{dst_host_ip});
    print "    <td>" . $ip . "</td>\n";
}
######
