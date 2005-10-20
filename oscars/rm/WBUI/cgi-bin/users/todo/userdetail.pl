## NOTE:  This is a subset of the original material.  This is here just to
##        to see what to add (i.e. syntax errors if you try to run this)

# title of the user account authorization notification email (sent to the user)
$authorization_txt = '[OSCARS Project] Your user account is ready for activation';

# email text encoding
$notification_email_encoding = 'ISO-8859-1';

##############################################################################
# print_userdetail
# set the processing result message to "please authorize this account" if
# necessary
#
sub print_userdetail {
    my( $form_params ) = @_;

    my( $msg );

    if ( $form_params->{pending_level} != 0 ) {
        $msg = 'This user account is authorized and pending user activation.' .
            ' <br>The level will be updated once the user activates the' .
            ' account. The pending user level is ' .
            $form_params->{pending_level} . '.';
    }
    else {
        $msg = 'This user account needs admin authorization.<br>To' .
            ' authorize a user, please add "admin" to the user level .';
    }

    # If the account is the "admin" account or the same as the one that's
    # logged in right now, it should not be deleted.
    if (($form_params->{user_dn} eq $admin_dn) ||
        ($form_params{user_dn} eq $form_params{current_loggedin_name})) {
	foreach $field ( @fields_to_display )
	{
            if ( $field eq 'level' ) {
            # For the user level field, the select tag is used... this
            #     complicates the regex a little bit.
            # (<select name=".."><option value=".." selected></select> to
            #     indicate the default value)
            # s///s: /s modifier makes the regex ignore new line (\n is
            # included in .) .*?AA: perform forward tracking until AA appears
            $html_line =~ s/(<select.*?name="$field".*?value=" .
                $form_params->{$field}")(.*?<\/select>)/$1 selected$2/is;
            }
            elsif ( $field eq 'register_datetime' ) {
                # create the duration string ("registered since...")
                my %TempDateTime;
                @TempDateTime{'year','month','day','hour','minute','second' } =
                    ( unpack( "a4aa2aa2aa2aa2aa2",
                      $form_params->{$field} ) )[0, 2, 4, 6, 8, 10];
                my $dtReg = DateTime->new( %TempDateTime, time_zone => 'UTC' );
                my $dtNow = DateTime->now( time_zone => 'UTC' );
                my $durObj = $dtNow - $dtReg;
                my @TempDuration = $durObj->in_units('years','months','days');
                my $TempDurString;
	        if ( $TempDuration[0] > 1 ) {
                    $TempDurString = $TempDuration[0] . " years ";
                }
	        if ( $TempDuration[0] == 1 ) {
                    $TempDurString .= $TempDuration[0] . " year ";
                }
	        if ( $TempDuration[1] > 1 ) {
                    $TempDurString .= $TempDuration[1] . " months ";
                }
	        if ( $TempDuration[1] == 1 ) {
                    $TempDurString .= $TempDuration[1] . " month ";
                }
 	        if ( $TempDuration[2] > 1 ) {
                    $TempDurString .= $TempDuration[2] . " days";
                }
		if ( $TempDuration[2] <= 1 ) {
                    $TempDurString .= $TempDuration[2] . " day";
                }
                $html_line =~ s/<!-- \(\(_Registration_Date_\)\) -->/
                    $form_params->{$field} UTC ($TempDurString)/g;
            }
            else {
                $html_line =~ s/(name="$field")/$1 value="
                    $form_params->{$field}"/i;
            }
	}
    }
}
######


##############################################################################
#
sub process_user_account_update {
    my( $form_params ) = @_;

    # see if this user's level has been changed from 0 to something else
    my $account_authorized = 0;

    if ( ( $form_params->{level} == 0 ) &&
         ( $form_params{level} != $user_profile_data{level} ) ) {
        $account_authorized = 1;
    }

    # if the user account has just been authorized, create an activation key
    # string and save the changed level as pending level
    my( $activation_key, $pending_level );

    if ( $account_authorized ) {
        $activation_Key = generate_random_string($account_activation_key_size);
        $pending_level = $form_params{level};
    }

    # if the user account has just been authorized, add these fields to the
    # fields/values to update, and do not compare the user level afterwards
    if ($account_authorized) {
        push( @fields_to_update, 'user_activation_key', 'user_pending_level' );
        push( @values_to_update, $activation_key, $pending_level );

        # 'level' is the last element of the array; remove it from the array
        $#fields_to_read--;
    }

    # If this account has just been authorized, send a notification email to
    # the user. Account activation instructions are included in the email.
    if ( $account_authorized ) {
        if (!open(MAIL,
            "|$sendmail_binary_path_and_flags $form_params->{email_primary}") {
            $error_msg = 'This user account has been' .
            ' authorized successfully, but sending a notification email to' .
            ' the user has failed. If the user does not receive the' .
            ' activation key by email, the account cannot be activated.' .
            ' Please check all the settings and the user\'s primary email' .
            ' address, and contact the webmaster at ' .
            $webmaster .
            ' and inform the person of the date and time of error.<br>' .
            '[Error] ' . $! );

        print MAIL 'From: ', $webmaster, "\n";
        print MAIL 'To: ', $form_params->{email_primary}, "\n";
        print MAIL 'Subject: ', $authorization_txt, "\n";
        print MAIL 'Content-Type: text/plain; charset="' .
                   $notification_email_encoding . '"' . "\n\n";
			
        print MAIL 'Your user account has been authorized by the' .
            ' administrator, and is ready for activation.', "\n";
        print MAIL 'Please visit the Web page below and activate your user' .
            ' account. If the URL appears in multiple lines, please copy' .
            ' and paste the whole address on your Web browser\'s address bar.' .
            "\n\n";

        print MAIL $account_activation_form_URI, "\n\n";

        print MAIL 'Your Login Name: ' . $form_params->{user_dn} . "\n";
        print MAIL 'Account Activation Key: ' . $activation_key . "\n";
        print MAIL 'Your User Level: ' . $user_level_description{$pending_level} .
                   ' (Lv ' . $pending_level . ')' . "\n";
        print MAIL 'Your password is the same as the one you specified' .
                   ' upon registration.' . "\n\n";

        print MAIL '---------------------------------------------------', "\n";
        print MAIL '=== This is an auto-generated e-mail ===', "\n";

        if (!close( MAIL )) {
           $error_msg = 'This user account' .
           ' has been authorized successfully, but sending a notification' .
           ' email to the user has failed. If the user does not receive the' .
           ' activation key by email, the account cannot be activated.' .
           ' Please check all the settings and the user\'s primary email' .
           ' address, and contact the webmaster at ' .
           $webmaster .
           ' and inform the person of the date and time of error.<br>' .
           '[Error] ' . $! );
        }
    }
    $status_msg = 'The user account information has been updated successfully.');
}
#####

##############################################################################
#
sub process_user_account_delete {
    my( $form_params ) = @_;

    # DB Query: delete the user profile data from the users table
    $query = "DELETE FROM users WHERE user_dn = ?";

    # DB Query: delete all the reservation records of the user from the reservations table as well
    $query = "DELETE FROM reservations WHERE user_dn = ?";

    $error_msg = 'The user account ' . $form_params->{user_dn} .
        ' has been deleted successfully, but an error has occurred while' .
        ' deleting reservations made by the user. Please contact the system' .
        ' administrator immediately to resolve the issue.<br>[Error] ' .
        $error_status ;

    $status_msg = 'The user account ' . $form_params{user_dn} .
        ' and all the reservations made by the user have been deleted' .
        ' successfully.';

}
######


sub process_password_reset {
    my( $form_params ) = @_;

    # DB Query: get the user's primary email address from the database
    $query = "SELECT user_email_primary FROM users WHERE user_dn = ?";

    $error_msg = 'An error has occurred while resetting the password for this' .
       ' user account.<br>[Error] ' . $error_status );

    # email the new password to the user
    if (!open(MAIL, "|$sendmail_binary_path_and_flags $user_email_primary")) {
        $error_msg = 'The password has been reset successfully, but emailing' .
           ' the new password to the user has failed. Please check all the' .
           ' settings and the user\'s primary email address, and reset the' .
           ' password again.<br>[Error] ' . $! );

        print MAIL 'From: ', $webmaster, "\n";
        print MAIL 'To: ', $user_email_primary, "\n";

        print MAIL 'Subject: ', $passwordreset_notification_email_title, "\n";
        print MAIL 'Content-Type: text/plain; charset="' .
                    $notification_email_encoding . '"' . "\n\n";

        print MAIL 'The password of your account has been reset by the system' .
                   ' administrator to the following.' . "\n";
        print MAIL 'Please change the password to your own once you log in to' .
                   ' the service.' . "\n\n";

        print MAIL 'Your Login Name: ', $form_params->{user_dn}, "\n";
        print MAIL 'New Password: ', $form_params->{password_new}, "\n\n";

        print MAIL '---------------------------------------------------', "\n";
        print MAIL '=== This is an auto-generated e-mail ===', "\n";

	if (!close( MAIL )) {
            $error_msg = 'The password has been reset successfully, but' .
               ' emailing the new password to the user has failed. Please' .
               ' check all the settings and the user\'s primary email' .
               ' address, and reset the password again.<br>[Error] ' . $! ;
        }

	$status_msg = 'The password for this user account has been' .
           ' successfully reset. The user will receive the new password in' .
           ' email shortly.';
}
####
