#!/usr/bin/perl

# adduser.pl:  Admin tool: Add a User page
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

# smiley icons used in the login name overlap check result page
%icon_locations = (
	'smile' => 'https://oscars.es.net/images/icon_biggrin.gif', # smile face
	'sad' => 'https://oscars.es.net/images/icon_sad.gif',   # sad face
	'exclaim' => 'https://oscars.es.net/images/icon_exclaim.gif' # "!" mark
);

# title of the user account authorization notification email (sent to the user)
$adminadduser_notification_email_title = '[OSCARS/BRUW] A new user account has been created for you';

# email text encoding (default: ISO-8859-1)
$adminadduser_notification_email_encoding = 'ISO-8859-1';


##### Beginning of mainstream #####


my (%form_params, %results);

my $cgi = CGI->new();
my $error_status = check_session_status(undef, $cgi);

if (!$error_status) {
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }
  $results{'error_msg'} = validate_params(\%form_params);
  ($error_status, %results) = soap_add_user(\%form_params);
  if (!$error_status) {
      update_frames("main_frame", "", $results{'status_msg'});
  }
  else {
      update_frames("main_frame", "", $results{'error_msg'});
  }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;


	&Process_User_Registration();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub validate_params
# In: None
# Out: None
sub validate_params
{
  my( $form_params ) = @_;

      # validate user input (Javascript also takes care of form data
      # validation)
  if ( $form_params->{'id'} =~ /\W|\s/) {
      return( "Please use only alphanumeric characters or _ for login name.");
  }
  if ( $form_params->{'dn'} eq '' ) {
      return( 'Please enter the desired login name.' );
  }
  if ( $form_params->{'dn'} =~ /\W|\s/ ) {
      return(  'Please use only alphanumeric characters or _ for login name.' );
  }
  if ( $form_params->{'password_once'} eq '' || $form_params->{'password_twice'} eq '' )
  {
      return( 'Please enter the password.' );
  }
  if ( $form_params->{'password_once'} ne $form_params->{'password_twice'} ) {
      return( 'Please enter the same password twice for verification.' );
  }

  return( "" );
}


##### sub process_user_pegistration
# In: None
# Out: None
sub process_user_registration
{
  my( $form_params ) = @_;

      # encrypt password
  my $encrypted_password = encode_passwd( $form_params->{'password_once'} );

      # create user account activation key
  my $activation_key = generate_random_string( $account_activation_key_size );

	### TODO:  connect to AAAS, get back result

	### send a notification email to the user (with account activation instruction)
  my $error_status = print_mail_message($form_params);
  if ($error_status) {
      return( 0, $error_status ); 

	### when everything has been processed successfully...
    # don't forget to show the user's login name
  return( 1, 'The new user account \'' . $form_params->{'dn'} . '\' has been created successfully. <br>The user will receive information on activating the account in email shortly.' );

}


sub print_mail_message
{
  my( $form_params ) = @_;
  my $status = open( MAIL, "|$sendmail_binary_path_and_flags $form_params->{'email_primary'}" )
  if (!defined($status)
  { 
    return( 'The user account has been created successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );
  }
  print MAIL 'From: ', $webmaster, "\n";
  print MAIL 'To: ', $form_params->{'email_primary'}, "\n";

  print MAIL 'Subject: ', $adminadduser_notification_email_title, "\n";
  print MAIL 'Content-Type: text/plain; charset="', $adminadduser_notification_email_encoding, '"', "\n\n";
		
  print MAIL 'A new user account has been created for you, and is ready for activation.', "\n";
  print MAIL 'Please visit the Web page below and activate your user account. If the URL appears in multiple lines, please copy and paste the whole address on your Web browser\'s address bar.', "\n\n";

  print MAIL $account_activation_form_URI, "\n\n";
  print MAIL 'Your Login Name: ', $form_params->{'dn'}, "\n";
  print MAIL 'Account Activation Key: ', $Activation_Key, "\n";
  print MAIL 'Password: ', $form_params->{'password_once'}, "\n";
  print MAIL 'Your User Level: ', $user_level_description{$form_params->{'level'}}, ' (Lv ', $form_params->{'level'}, ')', "\n";
  print MAIL 'Please change the password to your own once you activate the account.', "\n\n";

  print MAIL '---------------------------------------------------', "\n";
  print MAIL '=== This is an auto-generated e-mail ===', "\n";

  my $error_status = close( MAIL );
  if (!defined($error_status))
  {
    return(  'The user account has been created successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );
  }
  return "";
}

