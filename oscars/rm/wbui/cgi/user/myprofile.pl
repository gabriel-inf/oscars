#!/usr/bin/perl

# myprofile.pl:  Main service: My Profile page
# Last modified: April 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

my $Error_Status = &Process_Profile_Update();
if ( !$Error_Status)
{
    &Print_Frames();
}
else
{
    &Print_Status_Message($Error_Status);
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Process_Profile_Update
# In: None
# Out: Status Message
sub Process_Profile_Update
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'password_current'} eq '' )
	{
		return( 0, 'Please enter the current password.' );
	}

	my $Encrypted_Password;
	my $Update_Password = 0;

	if ( $FormData{'password_new_once'} ne '' || $FormData{'password_new_twice'} ne '' )
	{
		if ( $FormData{'password_new_once'} ne $FormData{'password_new_twice'} )
		{
			return( 0, 'Please enter the same new password twice for verification.' );
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
	return( 1, 'Your account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update

##### End of sub routines #####

##### End of script #####
