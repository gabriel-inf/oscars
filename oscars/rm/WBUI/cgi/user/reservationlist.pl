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

# Receive data from HTML form (accept both POST and GET methods)
%FormData = &Parse_Form_Input_Data( 'all' );

# login URI
$login_URI = 'https://oscars.es.net/';

#if (!(Verify_Login_Status('', undef))) 
#{
    #print "Location: $login_URI\n\n";
    #exit;
#}

( $Error_Status, %Results ) = soap_get_reservations(\%FormData, \@Fields_to_Display);
print STDERR Dumper(%Results);

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
  my $results = @_;

  print "Content-type: text/html\n\n";
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/user/reservationlist.js\"></script>\n";
  print "</head>\n\n";

  print "<body>\n\n";

  print "<script language=\"javascript\">print_navigation_bar('reservationList');</script>\n\n";

  print "<div>\n\n";

  print "<h2>View Active Reservations</h2>\n";
  print "<p>Click the Reservation ID number link to view detailed information about the reservation.\n";
  print "<br>Your reservations will appear highlighted in the table below.\n";
  print "</p>\n\n";

  print "<p>For inquiries, please contact the project administrator.</p>\n";

  print "</div>\n";

  print "<script language=\"javascript\">print_footer();</script>\n";
  print "</body>\n";
  print "</html>\n";
}


##### End of sub routines #####

##### End of script #####
