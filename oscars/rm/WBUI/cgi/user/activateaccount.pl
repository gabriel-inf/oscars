#!/usr/bin/perl -w

# activateaccount.pl:  Account Activation page
# Last modified: April 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use AAAS::Client::SOAPClient;

require '../lib/general.pl';


# on success loads accactivated.html

my ($error_status, $cgi, %results);

($error_status, $cgi) = check_login(0);

if (!$error_status) {
  ($error_status, %results) = process_user_account_activation(\$cgi->param);

  if ( !$error_Status ) {
      update_status_frame($results{'status_msg'};
  }
  else {
      update_status_frame($results{'error_msg'});
  }
}


exit;


##### sub process_user_account_activation
# In:  ref to form parameter hash
# Out: status, message
sub process_user_account_activation
{
  my( $form_params ) = @_;
  my(%results);

    # validate user input (just check for empty fields)
  if ( $form_params->{'dn'} eq '' ) {
      $results{'error_msg'} = 'Please enter your login name.';
      return( 1, %results );
  }

  if ( $form_params->{'activation_key'} eq '' ) {
      $results{'error_msg'} = 'Please enter the account activation key.';
      return( 1, %results );
  }

  if ( $form_params->{'password'} eq '' ) {
      $results{'error_msg'} = 'Please enter the password.';
      return( 1, %results );
  }

  ### start working with the database
  ### TODO:  call AAAS, get return info and value

  ### when everything has been processed successfully...
  # $Processing_Result_Message string may be anything, as long as it's not empty

  $results{'status_msg'} = 'The user account <strong>' . $form_params->{'dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
  return( 0, %results );

}
