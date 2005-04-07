#!/usr/bin/perl

# login.pl:  Main Service Login page
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# main service start point URI (the first screen a user sees after logging in)
$service_startpoint_URI = 'https://oscars.es.net/user/';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# if login successful, forward user to the next appropriate page
# all else: Update status but don't change main frame

my $Error_Status = &Process_User_Login();
if ( !$Error_Status )
{
    # forward the user to the main service page
    &Print_Frames($service_startpoint_URI, "Logged in as $FormData{'loginname'}");
}
else
{
    &Print_Status_Message($Error_Status);
}
exit;


##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Process_User_Login
# In: None
# Out: Error status
sub Process_User_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		return( 'Please enter your login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		return( 'Please enter your password.' );
	}

	### TODO:  call DB routine, get message back

        return ''

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
