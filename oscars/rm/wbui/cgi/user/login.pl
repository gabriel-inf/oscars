#!/usr/bin/perl

# login.pl:  Main Service Login page
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# main service start point URI (the first screen a user sees after logging in)
$main_service_startpoint_URI = 'https://oscars.es.net/user/reservation.phtml';

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
	if ( &Verify_Login_Status( ) == 1 )
	{
		# forward the user to the main service page
		&Update_Frames($main_service_startpoint_URI, 'Logged in');
		exit;
	}
	else
	{
		&Update_Status_Frame('Invalid login');
	}
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Process_User_Login
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Update_Status_Frame( 'Please enter your login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Update_Status_Frame( 'Please enter your password.' );
	}

	### TODO:  call DB routine, get message back

	### when everything has been processed successfully...
	&Update_Frames( $main_service_startpoint_URI , 'Login successful');

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
