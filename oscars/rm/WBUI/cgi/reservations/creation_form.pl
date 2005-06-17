#!/usr/bin/perl

# creation_form.pl:  form for making reservations
# Last modified: June 17, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;
use Data::Dumper;

require '../lib/general.pl';

my $cgi = CGI->new();
my ($dn, $user_level) = check_session_status(undef, $cgi);

if (!$dn) {
    print "Location:  https://oscars.es.net/\n\n";
}
else {
    print_reservation_form($user_level);
}
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

    print '<html>', "\n";
    print '<head>', "\n";
    print '  <style type="text/css" media="screen">', "\n";
    print '  <link rel="stylesheet" type="text/css" ';
    print '  href="https://oscars.es.net/styleSheets/layout.css">', "\n";
    print '  <script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
    print '  <script language="javascript" type="text/javascript" src="https://oscars.es.net/timeprint.js"></script>', "\n";
    print '  <script language="javascript" type="text/javascript" src="https://oscars.es.net/reservation.js"></script>', "\n";
    print '</head>', "\n";

    print '<body>', "\n";
    print '<script language="javascript">print_navigation_bar("', $user_level, '", "reservation");</script>', "\n";

    print '<div id="reservation_ui">', "\n";

    print '<form method="post" action="https://oscars.es.net/cgi-bin/reservations/create.pl" target="status_frame" onsubmit="return check_form(this);">', "\n";

    print '<input type="hidden" name="reservation_start_time">', "\n";

    print '<p>Please indicate the origin location and destination, along with the amount of bandwidth you would like to reserve (in Mbps).', "\n";
    print 'Origin and destination locations can be either host names or IP addresses. (IPv6 is not supported yet.)</p>', "\n";

    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Origin</th>', "\n";
    print '    <th>Destination</th>', "\n";
    print '    <th>Bandwidth</th>', "\n";
    print '  </tr>', "\n";
    print '  <tr>', "\n";
    print '    <td><input type="text" name="origin"></td>', "\n";
    print '    <td><input type="text" name="destination"></td>', "\n";
    print '    <td><input type="text" name="reservation_bandwidth" size="10"> Mbps</td>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    if (authorized($user_level, "engr")) {
        print '<p>Your account privileges allow you to explicitly specify the IP ', "\n";
        print 'address of the ingress and egress OSCARS loopbacks, and to ', "\n";
        print 'specify a persistent connection.</p>', "\n";

        print '<table>', "\n";
        print '  <tr>', "\n";
        print '    <th>LSP from</th>', "\n";
        print '    <th>LSP to</th>', "\n";
        print '    <th>Persistent</th>', "\n";
        print '  </tr>', "\n";
        print '  <tr>', "\n";
        print '    <td><input type="text" name="lsp_from"></td>', "\n";
        print '    <td><input type="text" name="lsp_to"></td>', "\n";
        print '    <td><input type="checkbox" name="persistent" value="0"></td>', "\n";
        print '  </tr>', "\n";
        print '</table>', "\n";
    }

    print '<br/>', "\n";
    print '<p>Below, please indicate the date and time that you want to start to', "\n";
    print 'use your reserved bandwidth.  The default time zone is the local time.</p>', "\n";

    print '<p>For testing, the reservation start time can be now.  For production use, the reservation', "\n";
    print 'start date and time should be at least two hours later than the current date and time.', "\n";
    print 'Any field left blank will default to the Example settings.</p>', "\n";
    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>&nbsp;</th>', "\n";
    print '    <th>year</th>', "\n";
    print '    <th>month</th>', "\n";
    print '    <th>date</th>', "\n";
    print '    <th>hour</th>', "\n";
    print '    <th>minute</th>', "\n";
    print '    <th>UTC offset</th>', "\n";
    print '  </tr>', "\n";
    print '  <tr class="alignright">', "\n";
    print '  <th>Start Time</th>', "\n";
    print '    <td><input type="text" name="start_year" size="4" maxlength="4"></td>', "\n";
    print '    <td><input type="text" name="start_month" size="2" maxlength="2"></td>', "\n";
    print '    <td><input type="text" name="start_date" size="2" maxlength="2"></td>', "\n";
    print '    <td><input type="text" name="start_hour" size="2" maxlength="2"></td>', "\n";
    print '    <td><input type="text" name="start_minute" size="2" maxlength="2"></td>', "\n";
    print '    <td><select name="start_timeoffset">', "\n";
    print '    <script language="javascript">print_timezone_offset();</script>', "\n";
    print '        <option value="+00:00">+00:00 (UTC)</option>', "\n";
    print '        <option value="+01:00">+01:00 (CET)</option>', "\n";
    print '        <option value="+02:00">+02:00 (EET)</option>', "\n";
    print '        <option value="+03:30">+03:30</option>', "\n";
    print '        <option value="+04:00">+04:00</option>', "\n";
    print '        <option value="+04:30">+04:30</option>', "\n";
    print '        <option value="+05:30">+05:30</option>', "\n";
    print '        <option value="+08:00">+08:00 (CCT)</option>', "\n";
    print '        <option value="+09:00">+09:00 (JST)</option>', "\n";
    print '        <option value="+09:30">+09:30 (ACST)</option>', "\n";
    print '        <option value="+10:00">+10:00 (GST)</option>', "\n";
    print '        <option value="+12:00">+12:00 (NZST)</option>', "\n";
    print '        <option value="-01:00">-01:00 (WAT)</option>', "\n";
    print '        <option value="-02:00">-02:00 (AT)</option>', "\n";
    print '        <option value="-03:00">-03:00</option>', "\n";
    print '        <option value="-03:30">-03:30</option>', "\n";
    print '        <option value="-04:00">-04:00 (AST)</option>', "\n";
    print '        <option value="-05:00">-05:00 (EST)</option>', "\n";
    print '        <option value="-06:00">-06:00 (CST)</option>', "\n";
    print '        <option value="-07:00">-07:00 (MST)</option>', "\n";
    print '        <option value="-08:00">-08:00 (PST)</option>', "\n";
    print '        <option value="-09:00">-09:00 (YST)</option>', "\n";
    print '        <option value="-10:00">-10:00 (AHST)</option>', "\n";
    print '        <option value="-11:00">-11:00 (NT)</option>', "\n";
    print '    </select></td>', "\n";
    print '  </tr>', "\n";
    print '  </tr>', "\n";
    print '  <tr class="alignright">', "\n";
    print '    <th>{Example}</th>', "\n";
    print '    <script language="javascript">print_start_datetime_example();</script>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    if (!authorized($user_level, "engr")) {
        print '<p>Please indicate the duration of the reservation, ', "\n";
        print 'starting from the above indicated date and time.  ', "\n";
        print 'Currently, fractional hours are permissible.</p>', "\n";
    }
    else {
        print '<p>Please indicate the duration of the reservation, ', "\n";
        print 'starting from the above indicated date and time.  ', "\n";
        print 'Currently, fractional hours are permissible.  If you ', "\n";
        print 'have specified a persistent connection, this ', "\n";
        print 'field is ignored.</p>', "\n";
    }
    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Duration:</th>', "\n";
    print '    <td>', "\n";
    print '      <input type="text" name="duration_hour" size="4" maxlength="4"> Hours', "\n";
    print '    </td>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    print '<p>Please let us know the purpose of making this reservation.</p>', "\n";
    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Description:</th>', "\n";
    print '    <td><textarea name="reservation_description" rows="3" cols="34"></textarea></td>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    print '<p><!-- Unless otherwise noted,  -->All fields except for the date and time are required for reservation.', "\n";
    print 'Please check the information that you filled in, and press the [Reserve the Resource] button below.</p>', "\n";

    print '<p>', "\n";
    print '<input type="submit" value="Reserve the Resource">', "\n";
    print '<input type="reset" value="Reset form fields">', "\n";
    print '</p>', "\n";

    print '</form>', "\n";
    print '</div>', "\n";

    print '<script language="javascript">print_footer();</script>', "\n";
    print '</body>', "\n";
    print '</html>', "\n";
}
######
