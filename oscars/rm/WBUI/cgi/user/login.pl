#!/usr/bin/perl -w

# login.pl:  Main Service Login page
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';
require 'soapclient.pl';


# main service start point URI (the first screen a user sees after logging in)
$Service_startpoint_URI = 'https://oscars.es.net/user/';


# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# if login successful, forward user to the next appropriate page
# all else: Update status but don't change main frame

my ($Error_Status, %Results) = &process_user_login();

if ( !$Error_Status )
{
    # forward the user to the main service page, after setting session
    if (!(&Verify_Login_Status($FormData, 1)))
    {
        &Update_Frames("", "Please try a different login name");
    } 
    else
    {
        &Update_Frames($Service_startpoint_URI, "Logged in as $FormData{'dn'}.", $FormData{'dn'});
    }
}
else
{
    &Update_Frames('', $Results{'error_msg'});
}
exit;



##### Beginning of sub routines #####


##### sub process_user_login
# In: None
# Out: Error status
sub process_user_login
{
  my(%results);

    # validate user input (just check for empty fields)
  if ( $FormData{'dn'} eq '' )
  {
      $results{'error_msg'} = 'Please enter your login name.';
      return( 1, %results );
  }

  if ( $FormData{'password'} eq '' )
  {
      $results{'error_msg'} = 'Please enter your password.';
      return( 1, %results );
  }
  my(%params);
  $params{'dn'} = $FormData{'dn'};
  $params{'password'} = &Encode_Passwd($FormData{'password'});

  return(soap_verify_login(\%params));
}


##### End of sub routines #####

##### End of script #####
