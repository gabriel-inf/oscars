#!/usr/bin/env perl

# gateway.pl:  Admin tool: Index page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';
require '../lib/authenticate.pl';

# template html file names
%interface_template_filename = (
	'registration' => 'register.html',
	'login' => 'login.html',
	'regprocessed' => 'regprocessed.html'
);

# admin tool start point URI (the first screen a user sees after logging in to the admin tool)
$admin_tool_startpoint_URI = 'userlist.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'adminregister': Process admin reservation & print screen with result output
# if 'mode' eq 'adminlogin': Process admin login & forward user to the next appropriate page
# all else (default): Check whether at least one admin account exists & print either admin register or login screen
if ( $FormData{'mode'} eq 'adminregister' )
{
	&Process_Admin_Registration();
}
elsif ( $FormData{'mode'} eq 'adminlogin' )
{
	&Process_Admin_Login();
}
else
{
	### Check whether the user has a valid login cookie
	if ( &Verify_Login_Status( $admin_login_cookie_name ) == 1 )
	{
		# forward the user to the admin tool gateway (login) screen
		print "Location: $admin_tool_startpoint_URI\n\n";
		exit;
	}
	else
	{
		### Check whether admin account is set up in the database
		my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

		# connect to the database
		( $Dbh, $Error_Status ) = &Database_Connect();
		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		# whether admin account exists (determine it with the level info, not the login name)
		$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_level'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $admin_user_level );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		&Query_Finish( $Sth );

		# disconnect from the database
		&Database_Disconnect( $Dbh );

		### proceed to the appropriate next action depending on the existence of the admin account
		if ( $Num_of_Affected_Rows == 0 )
		{
			# ID does not exist; go to admin registration
			&Print_Admin_Registration_Screen();
		}
		else
		{
			# ID exists; go to admin login
			&Print_Admin_Login_Screen();
		}
	}
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Admin_Registration_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Admin_Registration_Screen
{

	my( $Processing_Result, $Processing_Result_Message, $Template_HTML_File );
	
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
	
	if ( ( $Processing_Result == 1 ) && ( $Processing_Result_Message ne '' ) )
	{
		$Template_HTML_File = $interface_template_filename{'regprocessed'};
	}
	else
	{
		$Template_HTML_File = $interface_template_filename{'registration'};
	}

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

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
		}

		# if processing has failed for some reason, pre-fill the form with the %FormData values so that users do not need to fill the form again
		if ( $Processing_Result == 0 )
		{
			foreach $Key ( keys %FormData )
			{
				foreach ( $Html_Line )
				{
					s/(name="$Key")/$1 value="$FormData{$Key}"/ig;
				}
			}
		}

		print $Html_Line;
	}

	exit;

}
##### End of sub Print_Admin_Registration_Screen


##### sub Process_Admin_Registration
# In: None
# Out: None
# Calls sub Print_Admin_Registration_Screen at the end (with a success token)
sub Process_Admin_Registration
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'password_once'} eq '' || $FormData{'password_twice'} eq '' )
	{
		&Print_Admin_Registration_Screen( 0, 'Please enter the password.' );
	}
	elsif ( $FormData{'password_once'} ne $FormData{'password_twice'} )
	{
		&Print_Admin_Registration_Screen( 0, 'Please enter the same password twice for verification.' );
	}

	# encrypt password
	my $Encrypted_Password = &Encode_Passwd( $FormData{'password_once'} );

	# get current date/time string
	my $Current_DateTime = &Create_Time_String( 'dbinput' );

	### start working with the database
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

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# admin user level is set to 10 by default
	my @Stuffs_to_Insert = ( '', $admin_loginname_string, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 10, $Current_DateTime, '', 0 );

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_Admin_Registration_Screen( 0, 'An error occurred recording the admin account information on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Status );
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
	&Print_Admin_Registration_Screen( 1, 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.' );

}
##### End of sub Process_Admin_Registration


##### sub Print_Admin_Login_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Admin_Login_Screen
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
	
	if ( ( $Processing_Result == 1 ) && ( $Processing_Result_Message ne '' ) )
	{
		# forward the user to the initial admin tool screen
		print "Location: $admin_tool_startpoint_URI\n\n";
	}
	else
	{
		my $Template_HTML_File = $interface_template_filename{'login'};

		# open html template file
		open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
		my @Template_Html = <F_HANDLE>;
		close( F_HANDLE );

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
			}

			print $Html_Line;
		}
	}

	exit;

}
##### sub End of Print_Admin_Login_Screen


##### sub Process_Admin_Login
# In: None
# Out: None
# Calls sub Print_Admin_Login_Screen at the end (with a success token)
sub Process_Admin_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Admin_Login_Screen( 0, 'Please enter the login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Admin_Login_Screen( 0, 'Please enter the password.' );
	}

	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ? and $db_table_field_name{'users'}{'user_level'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'}, $admin_user_level );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# check whether this person has a valid admin privilege
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database or does not belong to the admin level
		&Print_Admin_Login_Screen( 0, 'Please check your login name and try again.' );
	}
	else
	{
		# this login name is in the database; compare passwords
		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			if ( $$Ref[0] eq &Encode_Passwd( $FormData{'password'} ) )
			{
				$Password_Match_Token = 1;
			}
		}
	}

	&Query_Finish( $Sth );

	### if the input password matched against the password from the database, set a logged-in cookie
	if ( $Password_Match_Token )
	{
		### now we will start writing to the database, so place a lock
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

		# delete any previously set random keys for the same login name
		$Query = "DELETE FROM $db_table_name{'cookiekey'} WHERE $db_table_field_name{'cookiekey'}{'user_loginname'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		&Query_Finish( $Sth );

		# create a random key
		my $Random_Key = &Generate_Randomkey( $FormData{'loginname'} );

		# insert the ramdon key and login name in the database
		$Query = "INSERT INTO $db_table_name{'cookiekey'} VALUES ( '', ?, ? )";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'}, $Random_Key );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		# get the cookiekey_id value (the last-inserted auto-increment field value)
		my $Cookiekey_ID = $Sth->{'mysql_insertid'};

		&Query_Finish( $Sth );

		# print the Set-Cookie browser header
		print &Set_Login_Cookie( 'login', $admin_login_cookie_name, $Cookiekey_ID, $FormData{'loginname'}, $Random_Key );

		# unlock the operation
		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		# disconnect from the database
		&Database_Disconnect( $Dbh );
	}
	else
	{
		&Database_Disconnect( $Dbh );
		&Print_Admin_Login_Screen( 0, 'Please check the password and try again.' );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Admin_Login_Screen( 1, 'The admin user has successfully logged in.' );

}
##### End of sub Process_Admin_Login


##### End of sub routines #####

##### End of script #####
