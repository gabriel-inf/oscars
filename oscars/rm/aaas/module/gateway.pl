#!/usr/bin/perl

# gateway.pl:  DB operations having to do with logging in as admin
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of sub routines #####

sub Verify_Admin_Acct(FormData)
{
		### Check whether admin account is set up in the database
		my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

		# connect to the database
		( $Dbh, $Error_Status ) = &Database_Connect();
		if ( $Error_Status != 1 )
		{
			return( $Error_Status );
		}

		# whether admin account exists (determine it with the level info, not the login name)
		$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_level'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $admin_user_level );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		&Query_Finish( $Sth );

		# disconnect from the database
		&Database_Disconnect( $Dbh );

		### proceed to the appropriate next action depending on the existence of the admin account
		if ( $Num_of_Affected_Rows == 0 )
		{
			# ID does not exist; go to admin registration
			return();
		}
		else
		{
			# ID exists; go to admin login
			return();
		}
}

##### End of Verify_Admin_Acct

##### sub Process_Admin_Registration
# In: FormData
# Out: None
# Calls sub Print_Admin_Registration_Screen at the end (with a success token)
sub Process_Admin_Registration(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# TODO:  lock table

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# admin user level is set to 10 by default
	my @Stuffs_to_Insert = ( '', $admin_loginname_string, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 10, $Current_DateTime, '', 0 );

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release db lock
		$Error_Status =~ s/CantExecuteQuery\n//;
		return( 0, 'An error occurred recording the admin account information on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock the operation

	### when everything has been processed successfully...
	return( 1, 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.' );

}
##### End of sub Process_Admin_Registration

##### sub Process_Admin_Login
# In: FormData
# Out: None
# Calls sub Print_Admin_Login_Screen at the end (with a success token)
sub Process_Admin_Login(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ? and $db_table_field_name{'users'}{'user_level'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'}, $admin_user_level );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# check whether this person has a valid admin privilege
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database or does not belong to the admin level
		return( 0, 'Please check your login name and try again.' );
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
		# TODO:  lock table

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

		# print the Set-Cookie browser header
		print &Set_Login_Cookie( 'login', $admin_login_cookie_name, $Cookiekey_ID, $FormData{'loginname'}, $Random_Key );

		# TODO: unlock the table

		# disconnect from the database
		&Database_Disconnect( $Dbh );
	}
	else
	{
		&Database_Disconnect( $Dbh );
		return( 0, 'Please check the password and try again.' );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 1, 'The admin user has successfully logged in.' );

}
##### End of sub Process_Admin_Login


##### End of sub routines #####

##### End of script #####
