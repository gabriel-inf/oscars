# title of the user account authorization notification email (sent to the user)
$authorization_notification_email_title = '[OSCARS Project] Your user account is ready for activation';

# title of the password reset notification email (sent to the user)
$passwordreset_notification_email_title = '[OSCARS MPLS Project] Your account password has been reset';

# email text encoding (default: ISO-8859-1)
$notification_email_encoding = 'ISO-8859-1';

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
