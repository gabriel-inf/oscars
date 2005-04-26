#!/usr/bin/perl

# userlist.pl:  Admin tool: User List page
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;

# template html file names
%interface_template_filename = (
	'userlist' => 'userlist.html',
	'useraccdetail' => 'userdetail.html'
);

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

# title of the user account authorization notification email (sent to the user)
$authorization_notification_email_title = '[Internet2 MPLS Project] Your user account is ready for activation';

# title of the password reset notification email (sent to the user)
$passwordreset_notification_email_title = '[Internet2 MPLS Project] Your account password has been reset';

# email text encoding (default: ISO-8859-1)
$notification_email_encoding = 'ISO-8859-1';


##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

# login URI
$login_URI = 'https://oscars.es.net/admin/';
$auth = AAAS::Client::Auth->new();

if (!($auth->verify_login_status(\%FormData, undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}


# if 'mode' eq 'useraccdetail': Print user account details
# if 'mode' eq 'useraccupdate': Update the user account info & print the updated user account details
# all else (default): Print user accounts list in table
if ( $FormData{'mode'} eq 'useraccdetail' )
{
	&Print_User_Account_Detail();
}
elsif ( $FormData{'mode'} eq 'useraccupdate' )
{
	&Process_User_Account_Update();
}
elsif ( $FormData{'mode'} eq 'useraccdelete' )
{
	&Process_User_Account_Delete();
}
elsif ( $FormData{'mode'} eq 'userpasswdreset' )
{
	&Process_User_Account_Password_Reset();
}
else
{
	&Print_User_Accounts_List();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_User_Accounts_List
# In: None
# Out: None (exits the program at the end)
sub Print_User_Accounts_List
{

	my( $Processing_Result, $Processing_Result_Message );
	
	if ( $#_ >= 0 )
	{
		$Processing_Result = $_[0];
		$Processing_Result_Message = $_[1];
	}
	else
	{
		$Processing_Result = 1;
		$Processing_Result_Message = '';
	}

	my $Template_HTML_File = $interface_template_filename{'userlist'};

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	### get the user list from the database and populate the table tag
	my( $Dbh, $Sth, $Error_Status, $Query );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# DB Query: get the user list
	# CAUTION: do not change the elements order of this array!!
	my @Fields_to_Read = ( 'user_register_id', 'user_dn', 'user_firstname', 'user_lastname', 'user_email_primary', 'user_level', 'user_pending_level' );

	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		$Query .= $db_table_field_name{'users'}{$_} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	$Query .= " FROM $db_table_name{'users'} ORDER BY $db_table_field_name{'users'}{'user_register_id'}";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# populate %User_Profile_Data with the data fetched from the database
	my %User_Profile_Data;
	@User_Profile_Data{@Fields_to_Read} = ();
	$Sth->bind_columns( map { \$User_Profile_Data{$_} } @Fields_to_Read );

	my $User_List_Table;

	while ( $Sth->fetch() )
	{
		my @User_List_Table_Row;

		# the last element of the array @Fields_to_Read is the pending user level
		my $User_Pending_Level = $User_Profile_Data{'user_pending_level'};

		# iterate through @Fields_to_Read except for its last element
		foreach $_ ( 0 .. ( $#Fields_to_Read - 1 ) )
		{
			# if the cell content is login name, surround it with a link to the user profile update page
			# the second SELECTed field is login name
			if ( $Fields_to_Read[$_] eq 'user_dn' )
			{
				push( @User_List_Table_Row, '<td><a href="?mode=useraccdetail&dn=' . $User_Profile_Data{$Fields_to_Read[$_]} . '">' . $User_Profile_Data{$Fields_to_Read[$_]} . '</a></td>' );
			}
			else
			{
				push( @User_List_Table_Row, "<td>$User_Profile_Data{$Fields_to_Read[$_]}</td>" );
			}
		}

		# the last column of the table is user level
		if ( $User_List_Table_Row[$#User_List_Table_Row] =~ /<td>(\d+)<\/td>/ )
		{
			if ( $1 == 0 )
			{
				if ( $User_Pending_Level != 0 )
				{
					$User_List_Table_Row[$#User_List_Table_Row] = '<td>' . $user_level_description{'pending'} . ' (Lv ' . $User_Pending_Level . ')</td>';

					# do not highlight the row
					unshift( @User_List_Table_Row, '<tr>' );
				}
				else
				{
					$User_List_Table_Row[$#User_List_Table_Row] = '<td>' . $user_level_description{$1} . ' (Lv ' . $1 . ')</td>';

					# highlight the row if user level is 0
					unshift( @User_List_Table_Row, '<tr class="attention">' );
				}
			}
			else
			{
				$User_List_Table_Row[$#User_List_Table_Row] = '<td>' . $user_level_description{$1} . ' (Lv ' . $1 . ')</td>';

				# do not highlight the row
				unshift( @User_List_Table_Row, '<tr>' );
			}
		}

		push( @User_List_Table_Row, "</tr>\n" );
		
		$User_List_Table .= join( '', @User_List_Table_Row );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach $Html_Line ( @Template_Html )
	{
		foreach ( $Html_Line )
		{
			s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
			s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'current_loggedin_name'}/g;
			s/<!-- \(\(_User_List_\)\) -->/$User_List_Table/g;
		}

		print $Html_Line;
	}

	exit;

}
##### End of sub Print_User_Accounts_List


##### sub Print_User_Account_Detail
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_User_Account_Detail
{

	my( $Processing_Result, $Processing_Result_Message );
	
	if ( $#_ >= 0 )
	{
		$Processing_Result = $_[0];
		$Processing_Result_Message = $_[1];
	}
	else
	{
		$Processing_Result = 1;
		$Processing_Result_Message = '';
	}

	my $Template_HTML_File = $interface_template_filename{'useraccdetail'};

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	### get the user detail from the database and populate the profile form
	my( $Dbh, $Sth, $Error_Status, $Query );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# names of the fields to be displayed on the screen
	my @Fields_to_Display = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'register_datetime', 'level', 'pending_level' );

	# DB Query: get the user profile detail
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Display )
	{
		my $Temp = 'user_' . $_;
		$Query .= $db_table_field_name{'users'}{$Temp} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	$Query .= " FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# populate %User_Profile_Data with the data fetched from the database
	my %User_Profile_Data;
	@User_Profile_Data{@Fields_to_Display} = ();
	$Sth->bind_columns( map { \$User_Profile_Data{$_} } @Fields_to_Display );
	$Sth->fetch();

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	# join all the html lines into one, to make all the regex work easier
	my $Html_Line = join( '', @Template_Html );

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

	foreach ( $Html_Line )
	{
		s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
		s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'current_loggedin_name'}/g;
		s/<!-- \(\(_User_Login_Name_\)\) -->/$FormData{'dn'}/g;
		s/(name="dn")/$1 value="$FormData{'dn'}"/ig;
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

	print $Html_Line;

	exit;

}
##### End of sub Print_User_Account_Detail


##### sub Process_User_Account_Update
# In: None
# Out: None
# Calls sub Print_User_Account_Detail at the end (with a success token)
sub Process_User_Account_Update
{

	### Javascript takes care of form data validation; no validation here

	### begin working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# read the current user information from the database to decide which fields are being updated
	# 'level' should always be the last entry of the array (important for later procedures)
	my @Fields_to_Read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'level' );

	# DB Query: get the user profile detail
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		my $Temp = 'user_' . $_;
		$Query .= $db_table_field_name{'users'}{$Temp} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	$Query .= " FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# populate %User_Profile_Data with the data fetched from the database
	my %User_Profile_Data;
	@User_Profile_Data{@Fields_to_Read} = ();
	$Sth->bind_columns( map { \$User_Profile_Data{$_} } @Fields_to_Read );
	$Sth->fetch();

	&Query_Finish( $Sth );

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

	### update information in the database

	# determine which fields to update in the user profile table
	# @Fields_to_Update and @Stuffs_to_Update should be an exact match
	my( @Fields_to_Update, @Values_to_Update );

	# if the user account has just been authorized, add these fields to the fields/values to update, and do not compare the user level afterwards
	if ( $Account_Authorized )
	{
		push( @Fields_to_Update, $db_table_field_name{'users'}{'user_activation_key'}, $db_table_field_name{'users'}{'user_pending_level'} );
		push( @Values_to_Update, $Activation_Key, $Pending_Level );

		# 'level' is the last element of the array; remove it from the array
		$#Fields_to_Read--;
	}

	# compare the current & newly input user profile data and determine which fields/values to update
	foreach $_ ( @Fields_to_Read )
	{
		if ( $User_Profile_Data{$_} ne $FormData{$_} )
		{
			my $Temp = 'user_' . $_;
			push( @Fields_to_Update, $db_table_field_name{'users'}{$Temp} );
			push( @Values_to_Update, $FormData{$_} );
		}
	}

	# if there is nothing to update...
	if ( $#Fields_to_Update < 0 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		&Print_User_Account_Detail( 0, 'There is no changed information to update.' );
	}

	# prepare the query for database update
	$Query = "UPDATE $db_table_name{'users'} SET ";
	foreach $_ ( 0 .. $#Fields_to_Update )
	{
		$Query .= $Fields_to_Update[$_] . " = ?, ";
	}
	$Query =~ s/,\s$//;
	$Query .= " WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Values_to_Update, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Account_Detail( 0, 'An error has occurred while updating the user account information.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
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
##### End of sub Process_User_Account_Update


##### sub Process_User_Account_Delete
# In: None
# Out: None
# Calls sub Print_User_Accounts_List at the end (with a success token)
sub Process_User_Account_Delete
{

	### Javascript takes care of confirming the account deleting; nothing done here

	### begin working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# DB Query: delete the user profile data from the users table
	$Query = "DELETE FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Accounts_List( 0, 'An error has occurred while deleting the user account \'' . $FormData{'dn'} . '\'.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# DB Query: delete all the reservation records of the user from the reservations table as well
	$Query = "DELETE FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Accounts_List( 0, 'The user account \'' . $FormData{'dn'} . '\' has been deleted successfully, but an error has occurred while deleting reservations made by the user. Please contact the system administrator immediately to resolve the issue.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
	}

	### when everything has been processed successfully...
	&Print_User_Accounts_List( 1, 'The user account \'' . $FormData{'dn'} . '\' and all the reservations made by the user have been deleted successfully.' );

}
##### End of sub Process_User_Account_Delete


##### sub Process_User_Account_Password_Reset
# In: None
# Out: None
# Calls sub Print_User_Account_Detail at the end (with a success token)
sub Process_User_Account_Password_Reset
{

	### Javascript takes care of form data validation; just check empty field here
	if ( $FormData{'password_new'} eq '' )
	{
		&Print_User_Account_Detail( 0, 'Please enter the new password to reset to.' );
	}

	# encrypt password
	my $Encrypted_Password = &Encode_Passwd( $FormData{'password_new'} );

	### begin working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# DB Query: get the user's primary email address from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_email_primary'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	my $User_Email_Primary;

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		$User_Email_Primary = $$Ref[0];
	}

	&Query_Finish( $Sth );

	# DB Query: update the password
	$Query = "UPDATE $db_table_name{'users'} SET $db_table_field_name{'users'}{'user_password'} = ? WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $Encrypted_Password, $FormData{'dn'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_User_Account_Detail( 0, 'An error has occurred while resetting the password for this user account.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
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
##### End of sub Process_User_Account_Password_Reset

##### End of sub routines #####

##### End of script #####
