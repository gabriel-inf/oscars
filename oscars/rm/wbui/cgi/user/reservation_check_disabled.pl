#!/usr/bin/perl

# reservation_check_disabled.pl:  Main interface CGI program for network
#                                 resource reservation process
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name
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
}
##### End of sub Print_Interface_Screen


##### sub Process_Reservation
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Reservation
{

	### check if browser had Javascript disabled
	# assume that most other data validations were done by the Javascript on client browser

	### validate origin & destination IP addresses
	# supports only the IPv4 format at this moment
	# [future note] it might be nice if we could check whether the IP address is actually reachable here...
	foreach $_ ( 'origin', 'destination' )
	{
		if ( $FormData{$_} !~ /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/ )
		{
			&Print_Interface_Screen( 0, '[ERROR] Please provide an IPv4 IP address for the ' . $_ . ' location.' );
		}
	}

	if ( $FormData{'origin'} eq $FormData{'destination'} )
	{
		&Print_Interface_Screen( 0, '[ERROR] Please provide different IP addresses for origin and destination locations.' );
	}

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

=head1
        ## TODO:  connect to BSS and get back results

	&Print_Interface_Screen( 1, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
