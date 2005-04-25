#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';
require 'soapclient.pl';

use Data::Dumper;


# login URI
#$login_URI = 'https://oscars.es.net/';

    # prints error message indicating to go to the login page if can't verify
#if (!(Verify_Login_Status('', undef))) 
#{
    #exit;
#}

# TODO:  FIX, need to get from session
my %FormData;
$FormData{'dn'} = 'oscars';

    # names of the fields to be read and displayed on the screen
my @Fields_to_Read = ( 'start_time', 'end_time', 'created_time', 'bandwidth', 'resv_class', 'burst_limit', 'status', 'src_id', 'dst_id', 'dn', 'description' );

( $Error_Status, %Results ) = soap_get_reservations(\%FormData, \@Fields_to_Read);

if (!$Error_Status)
{
    print_reservation_detail(\%Results);
}
else
{
    Update_Frames("", $Results{'error_msg'});
}
exit;



##### sub print_reservation_detail
# In: 
# Out:
sub print_reservation_detail
{
  my ($results) = @_;

  print "Content-type: text/html\n\n";
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/user/reservationlist.js\"></script>\n";
  print "</head>\n\n";

  print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n\n";

  print "<script language=\"javascript\">print_navigation_bar('reservationList');</script>\n\n";

  print "<div id=\"zebratable_ui\">\n\n";

  print "<p><em>View Active Reservations</em><br>\n";
  print "<p>Click the Reservation ID number link to view detailed information about the reservation.\n";
  print "</p>\n\n";

  print "<table cellspacing=\"0\" id=\"reservationlist\">\n";
  print "  <thead>\n";
  print "  <tr>\n";
  print "    <td>Reservation ID #</td>\n";
  print "    <td>Login Name</td>\n";
  print "    <td>Origin</td>\n";
  print "    <td>Destination</td>\n";
  print "    <td>Bandwidth</td>\n";
  print "    <td>Start Time</td>\n";
  print "    <td>End Time</td>\n";
  print "  </tr>\n";
  print "  </thead>\n";
  print "  <tbody>\n";
  print "  </tbody>\n";
  print "</table>\n\n";

  print "<p>For inquiries, please contact the project administrator.</p>\n\n";

  print "</div>\n\n";

  print "<script language=\"javascript\">print_footer();</script>\n";
  print "</body>\n";
  print "</html>\n\n";
}



##### End of sub routines #####

##### End of script #####
