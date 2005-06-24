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

    print '<p>The source and destination can be either host names or IP ';
    print 'addresses (IPv6 is not supported yet).  ';
    print 'The bandwidth is given in Mbps.</p>', "\n";

    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Source</th>', "\n";
    print '    <th>Destination</th>', "\n";
    print '    <th>Bandwidth</th>', "\n";
    print '  </tr>', "\n";
    print '  <tr>', "\n";
    print '    <td><input type="text" name="src_address" size="30"></td>', "\n";
    print '    <td><input type="text" name="dst_address" size="30"></td>', "\n";
    print '    <td><input type="text" name="reservation_bandwidth" size="10"> Mbps</td>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    if (authorized($user_level, "engr")) {
        print '<br/>', "\n";
        print '<p>The following fields are optional:  ';
        print 'The first three are self descriptive. ';
        print '<strong>DSCP</strong> sets the differentiated services ';
        print 'code point.</p>', "\n";

        print '<table>', "\n";
        print '  <tr>', "\n";
        print '    <th>Source port</th>', "\n";
        print '    <th>Destination port</th>', "\n";
        print '    <th>Protocol</th>', "\n";
        print '    <th>DSCP</th>', "\n";
        print '  </tr>', "\n";
        print '  <tr>', "\n";
        print '    <td><input type="text" name="reservation_src_port" ' .
                       'size="5"></td>' . "\n";
        print '    <td><input type="text" name="reservation_dst_port" ' .
                       'size="5"></td>' . "\n";
        print '    <td><input type="text" name="reservation_protocol" ' .
                       'size="4"></td>' . "\n";
        print '    <td><input type="text" name="reservation_dscp" ' .
                       ' size="2"></td>' . "\n";
        print '  </tr>', "\n";
        print '</table>', "\n";

        print '<br/>', "\n";
        print '<p>The optional <strong>Ingress loopback</strong> ';
        print 'and <strong>Egress loopback</strong> fields ';
        print 'indicate the IP addresses of the ingress and egress OSCARS ';
        print 'loopbacks.<br/>  <strong>WARNING</strong>:  Entries in these ';
        print 'fields may change default routing behavior for the selected ';
        print 'flow.</p>', "\n";

        print '<table>', "\n";
        print '  <tr>', "\n";
        print '    <th>Ingress loopback</th>', "\n";
        print '    <th>Egress loopback</th>', "\n";
        print '  </tr>', "\n";
        print '  <tr>', "\n";
        print '    <td><input type="text" name="lsp_from"></td>', "\n";
        print '    <td><input type="text" name="lsp_to"></td>', "\n";
        print '  </tr>', "\n";
        print '</table>', "\n";
    }

    print '<br/>', "\n";
    print '<p>Indicate the starting date and time, and the duration in hours, ';
    print 'of your reservation. ';
    print 'Fields left blank will default to the examples ';
    print 'below the input fields.  The default time zone is the local time.  ';
    if (authorized($user_level, "engr")) {
        print 'Checking the optional <strong>Persistent</strong> box makes ';
        print 'a reservation\'s duration indefinite, overriding ';
        print 'the duration field.</p>', "\n";
    }
    else {
        print '</p>', "\n";
    }
    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Year</th>', "\n";
    print '    <th>Month</th>', "\n";
    print '    <th>Date</th>', "\n";
    print '    <th>Hour</th>', "\n";
    print '    <th>Minute</th>', "\n";
    print '    <th>UTC offset</th>', "\n";
    print '    <th>Duration</th>', "\n";
    if (authorized($user_level, "engr")) {
        print '<th>Persistent</th>', "\n";
    }
    print '  </tr>', "\n";
    print '  <tr class="alignright">', "\n";
    print '    <td>';
    print '      <input type="text" name="start_year" size="4" maxlength="4">';
    print '    </td>', "\n";
    print '    <td>';
    print '      <input type="text" name="start_month" size="2" maxlength="2">';
    print '    </td>', "\n";
    print '    <td>';
    print '      <input type="text" name="start_date" size="2" maxlength="2">';
    print '    </td>', "\n";
    print '    <td>';
    print '      <input type="text" name="start_hour" size="2" maxlength="2">';
    print '    </td>', "\n";
    print '    <td>';
    print '      <input type="text" name="start_minute" size="2" maxlength="2">';
    print '    </td>', "\n";
    print '    <td>';
    print '    <select name="start_timeoffset">', "\n";
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
    print '    </select>';
    print '    </td>', "\n";
    print '    <td>', "\n";
    print '      <input type="text" name="duration_hour" size="4" ' .
                     'maxlength="4"> Hours' . "\n";
    print '    </td>', "\n";
    if (authorized($user_level, "engr")) {
        print '<td>', "\n";
        print '  <input type="checkbox" name="persistent" value="0">';
        print '</td>', "\n";
    }
    print '  </tr>', "\n";
    print '  <tr class="alignright">', "\n";
    print '    <script language="javascript">print_time_settings_example();</script>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    print '<br/>', "\n";
    print '<p>Please let us know the purpose of making this reservation.</p>', "\n";
    print '<table>', "\n";
    print '  <tr>', "\n";
    print '    <th>Description:</th>', "\n";
    print '    <td><textarea name="reservation_description" rows="2" cols="72"></textarea></td>', "\n";
    print '  </tr>', "\n";
    print '</table>', "\n";

    print '<br/>', "\n";
    print '<p>Fields are required unless otherwise indicated.</p>', "\n";
    print '<input type="submit" value="Reserve bandwidth">', "\n";
    print '<input type="reset" value="Reset form fields">', "\n";

    print '</form>', "\n";
    print '</div>', "\n";

    print '<script language="javascript">print_footer();</script>', "\n";
    print '</body>', "\n";
    print '</html>', "\n";
}
######
