#!/usr/bin/perl

# gateway.pl:  Admin tool: login script
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# login URI
$login_URI = 'https://oscars.es.net/admin/';
$auth = AAAS::Client::Auth->new();

if (!($auth->verify_login_status(\%FormData, undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}

# if 'mode' eq 'adminregister': Process admin reservation & print screen with result output
# if 'mode' eq 'adminlogin': Process admin login & forward user to the next appropriate page
# all else (default): Check whether at least one admin account exists & print either admin register or login screen
if ( $FormData{'mode'} eq 'adminregister' )
{
	&Process_Admin_Registration();
}
elsif ( $FormData{'mode'} eq 'adminlogin' )
{
	&Process_Admin_Login();
}
else
{
		### TODO:  contact AAAS to see whether admin account is set up,
                ###        get back status

		### proceed to the appropriate next action depending on the existence of the admin account
		if ( $Num_of_Affected_Rows == 0 )
		{
			# ID does not exist; go to admin registration
			&Print_Admin_Registration_Screen();
		}
		else
		{
			# ID exists; go to admin login
			&Print_Admin_Login_Screen();
		}
	}
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Admin_Registration_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Admin_Registration_Screen
{
}
##### End of sub Print_Admin_Registration_Screen


##### sub Process_Admin_Registration
# In: None
# Out: None
# Calls sub Print_Admin_Registration_Screen at the end (with a success token)
sub Process_Admin_Registration
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'password_once'} eq '' || $FormData{'password_twice'} eq '' )
	{
		&Print_Admin_Registration_Screen( 0, 'Please enter the password.' );
	}
	elsif ( $FormData{'password_once'} ne $FormData{'password_twice'} )
	{
		&Print_Admin_Registration_Screen( 0, 'Please enter the same password twice for verification.' );
	}

	# encrypt password
	my $Encrypted_Password = &Encode_Passwd( $FormData{'password_once'} );

        ### TODO:  contact AAAS, get results back

	### when everything has been processed successfully...
	&Print_Admin_Registration_Screen( 1, 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.' );

}
##### End of sub Process_Admin_Registration


##### sub Print_Admin_Login_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Admin_Login_Screen
{
}
##### sub End of Print_Admin_Login_Screen


##### sub Process_Admin_Login
# In: None
# Out: None
# Calls sub Print_Admin_Login_Screen at the end (with a success token)
sub Process_Admin_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'dn'} eq '' )
	{
		&Print_Admin_Login_Screen( 0, 'Please enter the login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Admin_Login_Screen( 0, 'Please enter the password.' );
	}

	### TODO:  contact AAAS, get results back

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Admin_Login_Screen( 1, 'The admin user has successfully logged in.' );

}
##### End of sub Process_Admin_Login


##### End of sub routines #####

##### End of script #####
