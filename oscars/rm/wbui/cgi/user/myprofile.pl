#!/usr/bin/perl

# myprofile.pl:  Main service: My Profile page
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/authenticate.pl';

# current script name (used for error message)
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

# if 'mode' eq 'updatemyprofile': Update the currently logged-in user's profile
# all else (default): Print the My Profile interface page
if ( $FormData{'mode'} eq 'updatemyprofile' )
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

		### TODO:  get the user detail from the database and populate the profile form
                ### TODO:  FIX
		foreach $Field ( @Fields_to_Display )
		{
			$Html_Line =~ s/(name="$Field")/$1 value="$User_Profile_Data{$Field}"/i;
		}
	}

	print $Html_Line;

	exit;

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

	### TODO:  make call to database, get status

        ### TODO:  output screen
	### when everything has been processed successfully...
	&Print_Interface_Screen( 1, 'Your account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update

##### End of sub routines #####

##### End of script #####
