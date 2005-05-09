#!/usr/bin/perl -w

# register.pl:  New user account registration
# Last modified: April 16, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;

require '../lib/general.pl';


# title of the new user registration notification email (sent to the admin)
$registration_notification_email_title = '[Internet2 MPLS Project] New User Registration Notice';

# where to send the notification email
# for multiple addresses, combine them with a comma (e.g. admin1@site.net, admin2@site.net)
$registration_notification_email_toaddr = 'dwrobertson@lbl.gov';

# email text encoding (default: ISO-8859-1)
$registration_notification_email_encoding = 'ISO-8859-1';


my (%form_params, %results);

my $cgi = CGI->new();
my $error_status = check_login(0, $cgi);

if (!$error_status) {
    foreach $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    ($error_status, %results) = process_user_registration(\%form_params);
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{'status_msg'});
    }
    else {
        update_frames($error_status, "main_frame", "", $results{'error_msg'});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;



##### sub process_user_registration
# In: None
# Out: Status message
sub process_user_registration
{
    my( $form_params ) = @_;

    # validate user input (fairly minimal... Javascript also takes care of form data validation)
    my(%results);
    if ( $form_params->{'dn'} eq '' ) {
        $results{'error_msg'} = 'Please enter your desired login name.';
        return( 1, %results );
    }
    elsif ( $form_params->{'dn'} =~ /\W|\s/ ) {
        $results{'error_msg'} = 'Please use only alphanumeric characters or _ for login name.';
        return( 1, %results );
    }

    if ( $form_params->{'password_once'} eq '' || $form_params->{'password_twice'} eq '' ) {
        $results{'error_msg'} = 'Please enter the password.';
        return( 1, %results );
    }
    elsif ( $form_params->{'password_once'} ne $form_params->{'password_twice'} ) {
        $results{'error_msg'} = 'Please enter the same password twice for verification.';
        return( 1, %results );
    }

    # encrypt password
    my $Encrypted_Password = &Encode_Passwd( $form_params->{'password_once'} );

    ## TODO:  contact the DB, get result back

    ### send a notification email to the admin
    open( MAIL, "|$sendmail_binary_path_and_flags $registration_notification_email_toaddr" ) || return( 1, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

    print MAIL 'From: ', $webmaster, "\n";
    print MAIL 'To: ', $registration_notification_email_toaddr, "\n";

    print MAIL 'Subject: ', $registration_notification_email_title, "\n";
    print MAIL 'Content-Type: text/plain; charset="', $registration_notification_email_encoding, '"', "\n\n";
		
    print MAIL $form_params->{'firstname'}, ' ', $form_params->{'lastname'}, ' <', $form_params->{'email_primary'}, '> has requested a new user account. Please visit the user admin Web page to accept or deny this request.', "\n\n";

    print MAIL 'Login Name: ', $form_params->{'dn'}, "\n\n";

    print MAIL 'Primary E-mail Address: ', $form_params->{'email_primary'}, "\n";
    print MAIL 'Secondary E-mail Address: ', $form_params->{'email_secondary'}, "\n";
    print MAIL 'Primary Phone Number: ', $form_params->{'phone_primary'}, "\n";
    print MAIL 'Secondary Phone Number: ', $form_params->{'phone_secondary'}, "\n\n";

    print MAIL '---------------------------------------------------', "\n";
    print MAIL '=== This is an auto-generated e-mail ===', "\n";

  close( MAIL ) || return( 1, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

    ### when everything has been processed successfully...
    # don't forget to show the user's login name
  return( 0, 'Your user registration has been recorded successfully. Your login name is <strong>' . $form_params->{'dn'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );

}
