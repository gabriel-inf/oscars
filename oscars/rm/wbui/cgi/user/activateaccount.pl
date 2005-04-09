#!/usr/bin/perl

# activateaccount.pl:  Account Activation page
# Last modified: April 5, 2005
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
%FormData = &Parse_Form_Input_Data( 'post' );

my ($Error_Status, $Error_Message) = &Process_User_Account_Activation();
if ( !$Error_Status )
{
    &Update_Frames("", "Successful activation");
}
else
{
    &Update_Frames("", $Error_Message[0]);
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Process_User_Account_Activation
# In: None
# Out: None
sub Process_User_Account_Activation
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		return( 1, 'Please enter your login name.' );
	}

	if ( $FormData{'activation_key'} eq '' )
	{
		return( 1, 'Please enter the account activation key.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		return( 1, 'Please enter the password.' );
	}

	### start working with the database
        ### TODO:  call AAAS, get return info and value

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 0, 'The user account <strong>' . $FormData{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );

}
##### End of sub Process_User_Account_Activation

##### End of sub routines #####

##### End of script #####
