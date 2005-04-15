#!/usr/bin/perl -w

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: April 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries
require '../lib/general.pl';
require 'soapclient.pl';


# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# TODO:  FIX
#$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];

my ($Error_Status, %Results) = &process_reservation();

if (!$Error_Status)
{
    &Update_Frames("", "Reservation made for $FormData{'loginname'}");
}
else
{
    &Update_Frames("", $Results{'error_msg'});
}
exit;



##### Beginning of sub routines #####

	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	# print "Pragma: no-cache\n";
	# print "Cache-control: no-cache\n";
	# print "Content-type: text/html\n\n";
	# exit;



##### sub process_reservation
# In: None
# Out: None
sub process_reservation
{

  my(%results);

    # make bandwidth, date, and time values numeric
  foreach $_ ( 'bandwidth', 'start_year', 'start_month', 'start_date', 'start_hour', 'duration_hour' )
  {
      $FormData{$_} += 0;
  }

    ### TODO:  call other subsystem with FormData
    ### subsystem returns reservation id, success or error message

    ### print screen or set src location
  $results{'error_msg'} = 'so far does nothing';
  return( 1, %results );

}


##### End of sub routines #####

##### End of script #####
