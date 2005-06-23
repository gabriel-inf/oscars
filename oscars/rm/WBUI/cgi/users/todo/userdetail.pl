# title of the user account authorization notification email (sent to the user)
$authorization_notification_email_title = '[OSCARS Project] Your user account is ready for activation';

# email text encoding (default: ISO-8859-1)
$notification_email_encoding = 'ISO-8859-1';

# title of the user account authorization notification email (sent to the user)
$authorization_notification_email_title = '[OSCARS Project] Your user account is ready for activation';

sub print_userdetail
{
	# set the processing result message to "please authorize this account" if necessary
	if ( ( $Processing_Result == 1 ) && ( $User_Profile_Data{'level'} == 0 ) )
	{
		if ( $Processing_Result_Message ne '' )
		{
			$Processing_Result_Message .= '<br><br>';
		}

		if ( $User_Profile_Data{'pending_level'} != 0 )
		{
			$Processing_Result_Message .= 'This user account is authorized and pending user activation. <br>The level will be updated once the user activates the account. The pending user level is ' . $User_Profile_Data{'pending_level'} . '.';
		}
		else
		{
			$Processing_Result_Message .= 'This user account needs admin authorization.<br>To authorize a user, please change the user level from Lv 0 to a different [User] level.';
		}
	}

	# if the account is the "admin" account or the same as the one that's logged in right now, it should not be deleted
	# remove the account delete form & the password reset from from the template html
	if ( $FormData{'dn'} eq $admin_dn || $FormData{'dn'} eq $FormData{'current_loggedin_name'} )
	{
		$Html_Line =~ s/(?:<!-- \(\(_Account_Delete_Form_Begin_\)\) -->).*?(?:<!-- \(\(_Account_Delete_Form_End_\)\) -->)//is;
		$Html_Line =~ s/(?:<!-- \(\(_Password_Reset_Form_Begin_\)\) -->).*?(?:<!-- \(\(_Password_Reset_Form_End_\)\) -->)//is;
	}

	foreach $Field ( @Fields_to_Display )
	{
		if ( $Field eq 'level' )
		{
			# for the user level field, the select tag is used... this complicates the regex a little bit
			# (<select name=".."><option value=".." selected></select> to indicate the default value)
			# s///s: /s modifier makes the regex ignore new line (\n is included in .)
			# .*?AA: perform forward tracking until AA appears
			$Html_Line =~ s/(<select.*?name="$Field".*?value="$User_Profile_Data{$Field}")(.*?<\/select>)/$1 selected$2/is;
		}
		elsif ( $Field eq 'register_datetime' )
		{
			# for date/time calculation
			use DateTime;

			# create the duration string ("registered since...")
			my %TempDateTime;
			@TempDateTime{ 'year', 'month', 'day', 'hour', 'minute', 'second' } = ( unpack( "a4aa2aa2aa2aa2aa2", $User_Profile_Data{$Field} ) )[0, 2, 4, 6, 8, 10];
			my $dtReg = DateTime->new( %TempDateTime, time_zone => 'UTC' );
			my $dtNow = DateTime->now( time_zone => 'UTC' );
			my $durObj = $dtNow - $dtReg;
			my @TempDuration = $durObj->in_units( 'years', 'months', 'days' );
			my $TempDurString;
			$TempDurString .= $TempDuration[0] . " years "	if ( $TempDuration[0] > 1 );
			$TempDurString .= $TempDuration[0] . " year "	if ( $TempDuration[0] == 1 );
			$TempDurString .= $TempDuration[1] . " months "	if ( $TempDuration[1] > 1 );
			$TempDurString .= $TempDuration[1] . " month "	if ( $TempDuration[1] == 1 );
			$TempDurString .= $TempDuration[2] . " days"	if ( $TempDuration[2] > 1 );
			$TempDurString .= $TempDuration[2] . " day"		if ( $TempDuration[2] <= 1 );

			$Html_Line =~ s/<!-- \(\(_Registration_Date_\)\) -->/$User_Profile_Data{$Field} UTC ($TempDurString)/g;
		}
		else
		{
			$Html_Line =~ s/(name="$Field")/$1 value="$User_Profile_Data{$Field}"/i;
		}
	}
}
######


sub Process_User_Account_Update
{
	### see if this user's level has been changed from 0 to something else
	my $Account_Authorized = 0;

	if ( ( $User_Profile_Data{'level'} == 0 ) && ( $FormData{'level'} != $User_Profile_Data{'level'} ) )
	{
		$Account_Authorized = 1;
	}

	# if the user account has just been authorized, create an activation key string and save the changed level as pending level
	my( $Activation_Key, $Pending_Level );

	if ( $Account_Authorized )
	{
		$Activation_Key = &Generate_Random_String( $account_activation_key_size );
		$Pending_Level = $FormData{'level'};
	}

	# if the user account has just been authorized, add these fields to the fields/values to update, and do not compare the user level afterwards
	if ( $Account_Authorized )
	{
		push( @Fields_to_Update, $db_table_field_name{'users'}{'user_activation_key'}, $db_table_field_name{'users'}{'user_pending_level'} );
		push( @Values_to_Update, $Activation_Key, $Pending_Level );

		# 'level' is the last element of the array; remove it from the array
		$#Fields_to_Read--;
	}

        
	### if this account has just been authorized, send a notification email to the user
	# account activation instruction is included in the email
	if ( $Account_Authorized )
	{
		open( MAIL, "|$sendmail_binary_path_and_flags $FormData{'email_primary'}" ) || &Print_User_Account_Detail( 0, 'This user account has been authorized successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );

			print MAIL 'From: ', $webmaster, "\n";
			print MAIL 'To: ', $FormData{'email_primary'}, "\n";

			print MAIL 'Subject: ', $authorization_notification_email_title, "\n";
			print MAIL 'Content-Type: text/plain; charset="', $notification_email_encoding, '"', "\n\n";
			
			print MAIL 'Your user account has been authorized by the administrator, and is ready for activation.', "\n";
			print MAIL 'Please visit the Web page below and activate your user account. If the URL appears in multiple lines, please copy and paste the whole address on your Web browser\'s address bar.', "\n\n";

			print MAIL $account_activation_form_URI, "\n\n";

			print MAIL 'Your Login Name: ', $FormData{'dn'}, "\n";
			print MAIL 'Account Activation Key: ', $Activation_Key, "\n";
			print MAIL 'Your User Level: ', $user_level_description{$Pending_Level}, ' (Lv ', $Pending_Level, ')', "\n";
			print MAIL 'Your password is the same as the one you specified upon registration.', "\n\n";

			print MAIL '---------------------------------------------------', "\n";
			print MAIL '=== This is an auto-generated e-mail ===', "\n";

		close( MAIL ) || &Print_User_Account_Detail( 0, 'This user account has been authorized successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );
	}

	### when everything has been processed successfully...
	&Print_User_Account_Detail( 1, 'The user account information has been updated successfully.' );

}
#####


sub Process_User_Account_Delete
{
	# DB Query: delete the user profile data from the users table
	$Query = "DELETE FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	# DB Query: delete all the reservation records of the user from the reservations table as well
	$Query = "DELETE FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'user_dn'} = ?";

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Accounts_List( 0, 'The user account \'' . $FormData{'dn'} . '\' has been deleted successfully, but an error has occurred while deleting reservations made by the user. Please contact the system administrator immediately to resolve the issue.<br>[Error] ' . $Error_Status );
	}

	### when everything has been processed successfully...
	&Print_User_Accounts_List( 1, 'The user account \'' . $FormData{'dn'} . '\' and all the reservations made by the user have been deleted successfully.' );

}
######


sub Process_User_Account_Password_Reset
{
	# DB Query: get the user's primary email address from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_email_primary'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Account_Detail( 0, 'An error has occurred while resetting the password for this user account.<br>[Error] ' . $Error_Status );
	}

	### email the new password to the user
	open( MAIL, "|$sendmail_binary_path_and_flags $User_Email_Primary" ) || &Print_User_Account_Detail( 0, 'The password has been reset successfully, but emailing the new password to the user has failed. Please check all the settings and the user\'s primary email address, and reset the password again.<br>[Error] ' . $! );

		print MAIL 'From: ', $webmaster, "\n";
		print MAIL 'To: ', $User_Email_Primary, "\n";

		print MAIL 'Subject: ', $passwordreset_notification_email_title, "\n";
		print MAIL 'Content-Type: text/plain; charset="', $notification_email_encoding, '"', "\n\n";

		print MAIL 'The password of your account has been reset by the system administrator to the following.', "\n";
		print MAIL 'Please change the password to your own once you log in to the service.', "\n\n";

		print MAIL 'Your Login Name: ', $FormData{'dn'}, "\n";
		print MAIL 'New Password: ', $FormData{'password_new'}, "\n\n";

		print MAIL '---------------------------------------------------', "\n";
		print MAIL '=== This is an auto-generated e-mail ===', "\n";

	close( MAIL ) || &Print_User_Account_Detail( 0, 'The password has been reset successfully, but emailing the new password to the user has failed. Please check all the settings and the user\'s primary email address, and reset the password again.<br>[Error] ' . $! );

	### when everything has been processed successfully...
	&Print_User_Account_Detail( 1, 'The password for this user account has been successfully reset. The user will receive the new password in email shortly.' );

}
####
