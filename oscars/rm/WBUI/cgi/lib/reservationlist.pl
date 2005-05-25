#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: May 19, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use BSS::Client::SOAPClient;

require 'general.pl';


    # names of the fields to be read
my @fields_to_read = ( 'reservation_id', 'user_dn', 'reservation_start_time', 'reservation_end_time', 'reservation_status', 'src_hostaddrs_id', 'dst_hostaddrs_id' );

my (%form_params, %results);


my $cgi = CGI->new();
my ($dn, $user_level, $admin_required) = check_session_status(undef, $cgi);

if (!$error_status) {
    foreach $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    $form_params{'user_dn'} = $dn;
    $form_params{'admin_required'} = $admin_required;
        # The reservation id, if present, indicates a deletion
    if ($form_params{'reservation_id'}) {
        ($error_status, %results) = soap_delete_reservation(\%form_params, \@fields_to_read);
    }
    else {
        ($error_status, %results) = soap_get_reservations(\%form_params, \@fields_to_read);
    }
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{'status_msg'});
        print_reservations(\%form_params, \%results);
    }
    else {
        update_frames($error_status, "main_frame", "", $results{'error_msg'});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}


exit;



##### sub print_reservations
# In: 
# Out:
sub print_reservations
{
    my ( $form_params, $results ) = @_;
    my ( $rowsref, $row );

    $rowsref = $results->{'rows'};
    print "<html>\n";
    print "<head>\n";
    print "<link rel=\"stylesheet\" type=\"text/css\" ";
    print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
    print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
    print "</head>\n\n";

    print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n\n";

    if ($form_params->{'admin_required'}) {
        print "<script language=\"javascript\">print_admin_bar('reservationlist');</script>\n\n";
    }
    else {
        print "<script language=\"javascript\">print_navigation_bar('reservationlist');</script>\n\n";
    }

    print "<div id=\"zebratable_ui\">\n\n";

    print "<p><em>View Active Reservations</em><br>\n";
    print "<p>Click on the Reservation Tag link to view detailed information about the reservation.\n";
    print "</p>\n\n";

    print "<table cellspacing=\"0\" width=\"90%\" id=\"reservationlist\">\n";
    print "  <thead>\n";
    print "  <tr>\n";
    print "    <td >Tag</td>\n";
    if ($form_params->{'admin_required'}) {
        print "    <td >User</td>\n";
    }
    print "    <td >Start Time</td>\n";
    print "    <td >End Time</td>\n";
    print "    <td >Status</td>\n";
    print "    <td >Origin</td>\n";
    print "    <td >Destination</td>\n";
    print "    <td >Action</td>\n";
    print "  </tr>\n";
    print "  </thead>\n";

    print "  <tbody>\n";
    foreach $row (@$rowsref) {
        if ($row->{'reservation_status'} ne 'finished') {
            print "  <tr>\n";
            print_row($row, $form_params->{'admin_required'});
            print "  </tr>\n";
        }
    }
    print "  </tbody>\n";
    print "</table>\n\n";

    print "<p>For inquiries, please contact the project administrator.</p>\n\n";
    print "</div>\n\n";

    print "<script language=\"javascript\">print_footer();</script>\n";
    print "</body>\n";
    print "</html>\n\n";
}



sub print_row
{
    my( $row, $admin_view ) = @_;
    my( $tag, $seconds, $time_field, $time_tag, $ip );


    ($time_tag, $time_field) = get_time_str($row->{'reservation_start_time'});
    # ESnet hard wired for now in tag
    # TODO:  incremental ID at end if multiple ones in same minute
    $tag = 'OSCARS.' . $row->{'user_dn'} . '.' . $time_tag . '-' . $row->{'reservation_id'};
    print '    <td><a href="https://oscars.es.net/cgi-bin/user/resvdetail.pl?reservation_id=' . $row->{reservation_id} . '">' . $tag . '</a></td>' . "\n"; 
  
    if ($admin_view) {
        print "    <td>" . $row->{'user_dn'} . "</td>\n";
    }
    print "    <td>" . $time_field . "</td>\n";

    ($time_tag, $time_field) = get_time_str($row->{'reservation_end_time'});
    print "    <td>" . $time_field . "</td>\n";
    print "    <td>" . $row->{'reservation_status'} . "</td>\n";

    $ip = get_oscars_host($row->{'src_hostaddrs_id'});
    print "    <td>" . $ip . "</td>\n";
    $ip = get_oscars_host($row->{'dst_hostaddrs_id'});
    print "    <td>" . $ip . "</td>\n";
    print '    <td><a href="https://oscars.es.net/cgi-bin/lib/reservationlist.pl?reservation_id=' . $row->{reservation_id} . '">DELETE</a></td>' . "\n";
}
