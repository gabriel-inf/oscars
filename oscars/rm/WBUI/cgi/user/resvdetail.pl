#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List Detail
# Last modified: May 2, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use DateTime;
use Socket;
use CGI;
use Data::Dumper;


use BSS::Client::SOAPClient;

require '../lib/general.pl';



    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'dn', 'id', 'start_time', 'end_time', 'created_time', 'bandwidth', 'burst_limit', 'status', 'src_id', 'dst_id', 'description' );


my (%form_params, %results);


my $cgi = CGI->new();
my $error_status = check_login(0, $cgi);

if (!$error_status) {
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }
      # FIX:  hard-wired for testing
  $form_params{'dn'} = 'oscars';
  ($error_status, %results) = BSS::Client::SOAPClient::soap_get_resv_detail(\%form_params, \@fields_to_read);
  if (!$error_status) {
      update_frames("main_frame", "", $results{'status_msg'});
      print_reservation_detail(\%results);
  }
  else {
      update_frames("main_frame", "", $results{'error_msg'});
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
  my ( $results) = @_;

  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "</head>\n\n";

  print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n\n";

  print "<script language=\"javascript\">print_navigation_bar('reservationList');</script>\n\n";

  print "<div id=\"zebratable_ui\">\n\n";

  print "<p><em>View Reservation</em><br>\n";
  print "</p>\n\n";

  print "<table cellspacing=\"0\" width=\"90%\" id=\"reservationlist\">\n";

  ($time_tag, $time_field) = get_time_str($results->{'start_time'});
  print "  <tr ><td>Tag:  </td>\n";
  my $tag = 'OSCARS.' . $results->{'dn'} . '.' . $time_tag . "-" . $results->{'id'};
  print "    <td>" . $tag . "</td></tr>\n"; 

  print "  <tr ><td>Start Time:  </td><td>$time_field</td></tr>\n";

  ($time_tag, $time_field) = get_time_str($results->{'end_time'});
  print "  <tr ><td>End Time:  </td><td>$time_field</td></tr>\n";

  ($time_tag, $time_field) = get_time_str($results->{'created_time'});
  print "  <tr ><td>Created Time:  </td><td>$time_field</td></tr>\n";

  print "  <tr ><td>Bandwidth:  </td><td>$results->{'bandwidth'}</td></tr>\n";
  print "  <tr ><td>Burst Limit:  </td><td>$results->{'burst_limit'}</td></tr>\n";
  print "  <tr ><td>Status:  </td><td>$results->{'status'}</td></tr>\n";

  print "  <tr ><td>Origin:  </td><td>", get_oscars_host($results->{'src_ip'}), "</td></tr>\n";
  print "  <tr ><td>Destination:  </td><td>", get_oscars_host($results->{'dst_ip'}), "</td></tr>\n";

  print "  <tr ><td>Description:  </td><td>", $results->{'description'}, "</td></tr>\n";
  print "</table>\n";

  print '<br/><br/>';
  print '<a href="https://oscars.es.net/cgi-bin/user/reservationlist.pl">Back to reservations list</a>';

  print "<p>For inquiries, please contact the project administrator.</p>\n\n";

  print "</div>\n\n";

  print "<script language=\"javascript\">print_footer();</script>\n";
  print "</body>\n";
  print "</html>\n\n";
}

