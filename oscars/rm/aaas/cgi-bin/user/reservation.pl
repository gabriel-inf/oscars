#!/usr/bin/perl

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: April 1, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# check if the user is logged in
if ( &Verify_Login_Status( $user_login_cookie_name ) != 1 )
{
	# forward the user to the user login screen
	print "Location: $main_service_login_URI\n\n";
	exit;
}
else
{
	$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];
}

# if 'mode' eq 'reserve': Process reservation & print screen with result output
#                         (print screen subroutine is called at the end of reservation process)
# all else (default): Print screen for user input
if ( $FormData{'mode'} eq 'reserve' )
{
	&Process_Reservation();
}
else
{
	&Print_Interface_Screen();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{

	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";
	exit;

}
##### End of sub Print_Interface_Screen


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

	# convert 12 am to 0 am
	if ( ( $FormData{'start_ampm'} eq 'am' ) && ( $FormData{'start_hour'} == 12 ) )
	{
		$FormData{'start_hour'} = 0;
	}
	### TODO:  call other subsystem with FormData
        ### subsystem returns reservation id, success or error message

        ### print screen or set src location

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
