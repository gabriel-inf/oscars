#!/usr/bin/perl

# editprofile.pl:  Admin tool: Edit Admin Profile page
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

# check if the user is logged in
if ( &Verify_Login_Status( $admin_login_cookie_name ) != 1 )
{
	# forward the user to the admin tool gateway (login) screen
	print "Location: $admin_tool_gateway_URI\n\n";
	exit;
}
else
{
	$FormData{'loginname'} = ( &Read_Login_Cookie( $admin_login_cookie_name ) )[1];
}

# if 'mode' eq 'admineditprofile': Update the currently logged in admin user's profile
# all else (default): Print the edit profile interface page
if ( $FormData{'mode'} eq 'admineditprofile' )
{
	&Process_Profile_Update();
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
    ## TODO:  connect to AAAS and get back result
}
##### End of sub Print_Interface_Screen


##### sub Process_Profile_Update
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Profile_Update
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'password_current'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the current password.' );
	}

	my $Encrypted_Password;
	my $Update_Password = 0;

	if ( $FormData{'password_new_once'} ne '' || $FormData{'password_new_twice'} ne '' )
	{
		if ( $FormData{'password_new_once'} ne $FormData{'password_new_twice'} )
		{
			&Print_Interface_Screen( 0, 'Please enter the same new password twice for verification.' );
		}
		else
		{
			# encrypt the new password
			$Encrypted_Password = &Encode_Passwd( $FormData{'password_new_once'} );

			$Update_Password = 1;
		}
	}

	### TODO:  connect to AAAS, and get back result
	### when everything has been processed successfully...
	&Print_Interface_Screen( 1, 'The account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update

##### End of sub routines #####

##### End of script #####
