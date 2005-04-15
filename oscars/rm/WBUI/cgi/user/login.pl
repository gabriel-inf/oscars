#!/usr/bin/perl -w

# login.pl:  Main Service Login page
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require 'soapclient.pl';

# main service start point URI (the first screen a user sees after logging in)
$Service_startpoint_URI = 'https://oscars.es.net/user/';

##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# if login successful, forward user to the next appropriate page
# all else: Update status but don't change main frame

my ($Error_Status, @Error_Message) = &process_user_login();

if ( !$Error_Status )
{
    # forward the user to the main service page
    &Update_Frames($Service_startpoint_URI, "Logged in as $FormData{'loginname'}.");
}
else
{
    &Update_Frames('', $Error_Message[0]);
}
exit;


##### End of mainstream #####


##### Beginning of sub routines #####


##### sub process_user_login
# In: None
# Out: Error status
sub process_user_login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		return( 1, 'Please enter your login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		return( 1, 'Please enter your password.' );
	}
        my(%params);
        $params{'loginname'} = $FormData{'loginname'};
        $params{'password'} = &Encode_Passwd($FormData{'password'});

        return(soap_process_user_login(%params));

}
##### End of sub process_user_login


##### End of sub routines #####

##### End of script #####
