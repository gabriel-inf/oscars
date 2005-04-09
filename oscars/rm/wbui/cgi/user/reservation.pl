#!/usr/bin/perl

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: April 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# TODO:  FIX
#$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];

my ($Error_Status, $Error_Message) = &Process_Reservation();

if (!$Error_Status)
{
    &Update_Frames("", "Reservation made for $FormData{'loginname'}");
}
else
{
    &Update_Frames("", $Error_Message[0]);
}
exit;

##### End of mainstream #####


##### Beginning of sub routines #####

	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	# print "Pragma: no-cache\n";
	# print "Cache-control: no-cache\n";
	# print "Content-type: text/html\n\n";
	# exit;



##### sub Process_Reservation
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Reservation
{

	# make bandwidth, date, and time values numeric
	foreach $_ ( 'bandwidth', 'start_year', 'start_month', 'start_date', 'start_hour', 'duration_hour' )
	{
		$FormData{$_} += 0;
	}

	### TODO:  call other subsystem with FormData
        ### subsystem returns reservation id, success or error message

        ### print screen or set src location
        return(1, "so far does nothing");

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
