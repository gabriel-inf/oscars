#!/usr/bin/perl -w

# activateaccount.pl:  Account Activation page
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

# on success loads accactivated.html


# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# login URI
$login_URI = 'https://oscars.es.net/';

$auth = AAAS::Client::Auth->new();
if (!($auth->verify_login_status(\%FormData, undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}

my ($Error_Status, %Results) = &process_user_account_activation();

if ( !$Error_Status ) {
  &Update_Frames("", $Results{'status_msg'};
}
else {
  &Update_Frames("", $Results{'error_msg'});
}

exit;


##### Beginning of sub routines #####


##### sub process_user_account_activation
# In: None
# Out: None
sub process_user_account_activation
{
  my(%results);

    # validate user input (just check for empty fields)
  if ( $FormData{'dn'} eq '' ) {
      $results{'error_msg'} = 'Please enter your login name.';
      return( 1, %results );
  }

  if ( $FormData{'activation_key'} eq '' ) {
      $results{'error_msg'} = 'Please enter the account activation key.';
      return( 1, %results );
  }

  if ( $FormData{'password'} eq '' ) {
      $results{'error_msg'} = 'Please enter the password.';
      return( 1, %results );
  }

  ### start working with the database
  ### TODO:  call AAAS, get return info and value

  ### when everything has been processed successfully...
  # $Processing_Result_Message string may be anything, as long as it's not empty

  $results{'status_msg'} = 'The user account <strong>' . $FormData{'dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
  return( 0, %results );

}


##### End of sub routines #####

##### End of script #####
