#!/usr/bin/perl

# login.pl:  login interaction with DB
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'lib/general.pl';
require 'database.pl';

##### sub Process_User_Login
# In: FormData
# Out: status message
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Login(FormData)
{
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# check whether this person is a registered user
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
		&Database_Disconnect( $Dbh );
		return( 0, 'Please check your login name and try again.' );
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
				return( 0, 'This account is not authorized or activated yet.' );
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
				return( $Error_Status );
			}
		}

		# delete any previously set random keys for the same login name
		$Query = "DELETE FROM $db_table_name{'cookiekey'} WHERE $db_table_field_name{'cookiekey'}{'user_loginname'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
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
			return( $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'}, $Random_Key );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
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
		return( 0, 'Please check your password and try again.' );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 1, 'The user has successfully logged in.' );

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
