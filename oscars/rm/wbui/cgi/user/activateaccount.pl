#!/usr/bin/perl

# activateaccount.pl:  Account Activation page
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# on success loads accactivated.html

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'login': Process login & forward user to the next appropriate page
# all else (default): Print user login screen
if ( $FormData{'mode'} eq 'activate' )
{
	&Process_User_Account_Activation();
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
##### sub End of Print_Interface_Screen


##### sub Process_User_Account_Activation
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Account_Activation
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter your login name.' );
	}

	if ( $FormData{'activation_key'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the account activation key.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the password.' );
	}

	### start working with the database
        ### TODO:  call AAAS, get return info and value

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Interface_Screen( 1, 'The user account <strong>' . $FormData{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );

}
##### End of sub Process_User_Account_Activation

##### End of sub routines #####

##### End of script #####
