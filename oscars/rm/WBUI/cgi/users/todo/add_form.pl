# title of the user account authorization notification email (sent to the user)
$adminadduser_notification_email_title = '[OSCARS/BRUW] A new user account has been created for you';

##############################################################################
# validate_params
# In: None
# Out: None
#
sub validate_params {
    my( $form_params ) = @_;

    # validate user input (Javascript also takes care of form data validation)
    if ($form_params->{id} =~ /\W|\s/) {
        return( 'Please use only alphanumeric characters or _ for login name.' );
    }
    if (!$form_params->{dn}) {
        return( 'Please enter the desired login name.' );
    }
    if ($form_params->{dn} =~ /\W|\s/ ) {
        return( 'Please use only alphanumeric characters or _ for login name.' );
    }
    if (!$form_params->{password_once} ||
         !$form_params->{'password_twice'}) {
        return(' Please enter the password. ');
    }
    if ($form_params->{password_once} ne $form_params->{password_twice}) {
        return(' Please enter the same password twice for verification.' );
    }
    return( "" );
}
######

##############################################################################
# process_user_pegistration
# In: None
# Out: None
#
sub process_user_registration {
    my( $form_params ) = @_;

    # encrypt password
    my $encrypted_password = encode_passwd($form_params->{password_once});

    # create user account activation key
    my $activation_key = generate_random_string($account_activation_key_size);

    # TODO:  connect to AAAS, get back result

    # send a notification email to the user (with account activation instruction)
    my $error_status = print_mail_message($form_params);
    if ($error_status) {
        return( 0, $error_status ); 
    }

    my $status_msg = 'The new user account ' . $form_params->{user_dn} .
               ' has been created successfully.  The user will receive ' .
               'information on activating the account in email shortly.';
}


sub print_mail_message {
    my( $form_params ) = @_;
    my( $msg, $status );

    $status = open( MAIL,
           "|$sendmail_binary_path_and_flags $form_params->{email_primary}" );
    if (!defined($status)) { 
        $msg = 'The user account has been created successfully, but sending ' .
           'a notification email to the user has failed. If the user does ' .
           'not receive the activation key by email, the account cannot be ' .
           'activated. Please check all the settings and the user\'s ' .
           'primary email address, and contact the webmaster at ' .
           $webmaster .
           ' and inform the person of the date and time of error.<br>' .
           '[Error] ' . $!;
        return( $msg );
    }
    print MAIL 'From: ', $webmaster, "\n";
    print MAIL 'To: ', $form_params->{'email_primary'}, "\n";

    print MAIL 'Subject: ', $adminadduser_notification_email_title, "\n";
    print MAIL 'Content-Type: text/plain; charset="' .
               $adminadduser_notification_email_encoding . '"' . "\n\n";
		
    print MAIL 'A new user account has been created for you, and is ready' .
           ' for activation.' . "\n" . 'Please visit the Web page below' .
           ' and activate your user account. If the URL appears in multiple' .
           ' lines, please copy and paste the whole address on your Web' .
           ' browser\'s address bar.' . "\n\n";

    print MAIL $account_activation_form_URI, "\n\n";
    print MAIL 'Your Login Name: ', $form_params->{user_dn}, "\n";
    print MAIL 'Account Activation Key: ', $Activation_Key, "\n";
    print MAIL 'Password: ', $form_params->{password_once}, "\n";
    print MAIL 'Your User Level: ' .
           $user_level_description{$form_params->{level}} .
           ' (Lv ' . $form_params->{level} . ')' . "\n";
    print MAIL 'Please change the password to your own once you activate '.
               'the account.' . "\n\n";

    print MAIL '---------------------------------------------------', "\n";
    print MAIL '=== This is an auto-generated e-mail ===', "\n";

    my $error_status = close( MAIL );
    if (!defined($error_status)) {
        $msg = 'The user account has been created successfully, but sending' .
            ' a notification email to the user has failed. If the user does' .
            ' not receive the activation key by email, the account cannot' . 
            ' be activated. Please check all the settings and the user\'s' .
            ' primary email address, and contact the webmaster at ' .
            $webmaster . ' and inform the person of the date and time of' .
            ' error.<br>[Error] ' . $!;
        return( $msg );
    }
    return( "" );
}

1;
