#!/usr/bin/perl

# login.pl:  Main Service Login page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# main service start point URI (the first screen a user sees after logging in)
$main_service_startpoint_URI = 'https://oscars.es.net/user/reservation.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'login': Process login & forward user to the next appropriate page
# all else (default): Print user login screen
if ( $FormData{'mode'} eq 'login' )
{
	&Process_User_Login();
}
else
{
	### Check whether the user has a valid login cookie
	if ( &Verify_Login_Status( $user_login_cookie_name ) == 1 )
	{
		# forward the user to the main service page
		print "Location: $main_service_startpoint_URI\n\n";
		exit;
	}
	else
	{
		&Print_Interface_Screen();
	}
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{
	exit;
}
##### sub End of Print_Interface_Screen


##### sub Process_User_Login
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter your login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the password.' );
	}

	### TODO:  call DB routine, get message back

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Interface_Screen( 1, 'The user has successfully logged in.' );

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
