#!/usr/bin/perl

# login.pl:  Main Service Login page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';
require '../lib/authenticate.pl';

# main service start point URI (the first screen a user sees after logging in)
$main_service_startpoint_URI = 'https://oscars.es.net/user/reservation.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'login': Process login & forward user to the next appropriate page
# all else (default): Print user login screen
if ( $FormData{'mode'} eq 'login' )
{
	&Process_User_Login();
}
else
{
	### Check whether the user has a valid login cookie
	if ( &Verify_Login_Status( $user_login_cookie_name ) == 1 )
	{
		# forward the user to the main service page
		print "Location: $main_service_startpoint_URI\n\n";
		exit;
	}
	else
	{
		&Print_Interface_Screen();
	}
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{
	exit;
}
##### sub End of Print_Interface_Screen


##### sub Process_User_Login
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Login
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter your login name.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the password.' );
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
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# check whether this person is a registered user
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
		&Database_Disconnect( $Dbh );
		&Print_Interface_Screen( 0, 'Please check your login name and try again.' );
	}
	else
	{
		# this login name is in the database; compare passwords
		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			if ( $$Ref[1] eq $non_activated_user_level )
			{
				# this account is not authorized & activated yet
				&Database_Disconnect( $Dbh );
				&Print_Interface_Screen( 0, 'This account is not authorized or activated yet.' );
			}
			elsif ( $$Ref[0] eq &Encode_Passwd( $FormData{'password'} ) )
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
				&Database_Disconnect( $Dbh );
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

		# print Set-Cookie browser header
		print &Set_Login_Cookie( 'login', $user_login_cookie_name, $Cookiekey_ID, $FormData{'loginname'}, $Random_Key );

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
		&Print_Interface_Screen( 0, 'Please check your password and try again.' );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Interface_Screen( 1, 'The user has successfully logged in.' );

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
