#!/usr/bin/perl -w

# register.pl:  New user account registration
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';	# required for sendmail location
require 'soapclient.pl';


# title of the new user registration notification email (sent to the admin)
$registration_notification_email_title = '[Internet2 MPLS Project] New User Registration Notice';

# where to send the notification email
# for multiple addresses, combine them with a comma (e.g. admin1@site.net, admin2@site.net)
$registration_notification_email_toaddr = 'dwrobertson@lbl.gov';

# email text encoding (default: ISO-8859-1)
$registration_notification_email_encoding = 'ISO-8859-1';



# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

my ($Error_Status, %Results) = &process_user_registration();
if ( !$Error_Status )
{
    &Update_Frames();
}
else
{
    &Update_Frames('', $Results{'error_msg'});
}

exit;



##### Beginning of sub routines #####

##### sub process_user_registration
# In: None
# Out: Status message
sub process_user_registration
{

    # validate user input (fairly minimal... Javascript also takes care of form data validation)
  my(%results);
  if ( $FormData{'loginname'} eq '' )
  {
      $results{'error_msg'} = 'Please enter your desired login name.';
      return( 1, %results );
  }
  elsif ( $FormData{'loginname'} =~ /\W|\s/ )
  {
      $results{'error_msg'} = 'Please use only alphanumeric characters or _ for login name.';
      return( 1, %results );
  }

  if ( $FormData{'password_once'} eq '' || $FormData{'password_twice'} eq '' )
  {
      $results{'error_msg'} = 'Please enter the password.';
      return( 1, %results );
  }
  elsif ( $FormData{'password_once'} ne $FormData{'password_twice'} )
  {
      $results{'error_msg'} = 'Please enter the same password twice for verification.';
      return( 1, %results );
  }

    # encrypt password
  my $Encrypted_Password = &Encode_Passwd( $FormData{'password_once'} );

        ## TODO:  contact the DB, get result back


    ### send a notification email to the admin
  open( MAIL, "|$sendmail_binary_path_and_flags $registration_notification_email_toaddr" ) || return( 1, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

  print MAIL 'From: ', $webmaster, "\n";
  print MAIL 'To: ', $registration_notification_email_toaddr, "\n";

  print MAIL 'Subject: ', $registration_notification_email_title, "\n";
  print MAIL 'Content-Type: text/plain; charset="', $registration_notification_email_encoding, '"', "\n\n";
		
  print MAIL $FormData{'firstname'}, ' ', $FormData{'lastname'}, ' <', $FormData{'email_primary'}, '> has requested a new user account. Please visit the user admin Web page to accept or deny this request.', "\n\n";

  print MAIL 'Login Name: ', $FormData{'loginname'}, "\n\n";

  print MAIL 'Primary E-mail Address: ', $FormData{'email_primary'}, "\n";
  print MAIL 'Secondary E-mail Address: ', $FormData{'email_secondary'}, "\n";
  print MAIL 'Primary Phone Number: ', $FormData{'phone_primary'}, "\n";
  print MAIL 'Secondary Phone Number: ', $FormData{'phone_secondary'}, "\n\n";

  print MAIL '---------------------------------------------------', "\n";
  print MAIL '=== This is an auto-generated e-mail ===', "\n";

  close( MAIL ) || return( 1, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

    ### when everything has been processed successfully...
    # don't forget to show the user's login name
  return( 0, 'Your user registration has been recorded successfully. Your login name is <strong>' . $FormData{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );

}


##### End of sub routines #####

##### End of script #####
