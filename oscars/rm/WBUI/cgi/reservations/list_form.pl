#!/usr/bin/perl

# list_form.pl:  page listing reservations
# Last modified: July 23, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;

use AAAS::Client::SOAPClient;
use Data::Dumper;

require '../lib/general.pl';


my( %form_params, $oscars_home );
my $cgi = CGI->new();

($form_params{user_dn}, $form_params{user_level},
 $oscars_home, $form_params{timezone_offset}) =
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

    my( $error_status, $results );

    $form_params->{method} = 'get_reservations';
    my $som = aaas_dispatcher($form_params);
    if ($som->faultstring) {
        update_status_frame(1, $som->faultstring);
        return;
    }
    $results = $som->result;
    print_reservations($results, $form_params->{user_level});
    update_status_frame(0, "Successfully retrieved reservations.");
}
######

##############################################################################
# print_reservations:  print list of all reservations if the caller has admin
#                    privileges, otherwise just print that user's reservations
# In:   form parameters, results of SOAP call
# Out:  None
#
sub print_reservations {
    my ( $results, $user_level ) = @_;

    my ( $rowsref, $row );

    $rowsref = $results->{rows};
    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="' . $oscars_home . 'styleSheets/layout.css">', "\n";
    print '    <script language="javascript" type="text/javascript"' .
               'src="' . $oscars_home . 'scripts/main_common.js"></script>' . "\n";
    print '    <script language="javascript" type="text/javascript"' .
               'src="' . $oscars_home . 'scripts/timeprint.js"></script>' . "\n";
    print '    <script language="javascript" type="text/javascript"' .
               'src="' . $oscars_home . 'scripts/sorttable.js"></script>', "\n";
    print '</head>', "\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n";
    print '<script language="javascript">';
    print '  print_navigation_bar("', $user_level, '", "reservationlist");';
    print '</script>', "\n";

    print '<div id="zebratable_ui">', "\n\n";

    print '<p>Click on a column header to sort by that column. ';
    print 'Times given are in the time zone of the browser. ', "\n";
    print 'Click on the Reservation Tag link to view detailed information ';
    print 'about the reservation. ', "\n";
    print '</p>', "\n\n";

    print '<p><form method="post" action="' . $oscars_home . 
              'cgi-bin/reservations/list_form.pl">' . "\n";
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
    print '    <a href="' . $oscars_home .
               'cgi-bin/reservations/details.pl?reservation_id=' .
               $row->{reservation_id} . '">';
    print      $row->{reservation_tag}; 
    print '    </a></td>', "\n";
  
    print '    <td>', "\n";
    print "    $row->{reservation_start_time}";
    print '    </td>', "\n";

    print '    <td>';
    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        print "    $row->{reservation_end_time}";
    }
    else {
        print 'PERSISTENT', "\n";
    }
    print '    </td>', "\n";

    print "    <td>" . $row->{reservation_status} . "</td>\n";

    print "    <td>" . $row->{src_address} . "</td>\n";
    print "    <td>" . $row->{dst_address} . "</td>\n";
}
######
