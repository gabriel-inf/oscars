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
		my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

		# connect to the database
		( $Error_Code, $Dbh ) = &Database_Connect();
		if ( $Error_Code )
		{
			return( 1, $Error_Code );
		}

		# whether admin account exists (determine it with the level info, not the login name)
		$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_level'} = ?";

		( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $admin_user_level );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
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
	my( $Dbh, $Sth, $Error_Code, $Query );

	# TODO:  lock table

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# admin user level is set to 10 by default
	my @Stuffs_to_Insert = ( '', $admin_loginname_string, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 10, $Current_DateTime, '', 0 );

	( $Error_Code, undef ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release db lock
		$Error_Code =~ s/CantExecuteQuery\n//;
		return( 1, 'An error occurred recording the admin account information on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Code );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock the operation

	### when everything has been processed successfully...
	return( 0, 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.' );

}
##### End of sub Process_Admin_Registration

##### sub Process_Admin_Login
# In: FormData
# Out: None
# Calls sub Print_Admin_Login_Screen at the end (with a success token)
sub Process_Admin_Login(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ? and $db_table_field_name{'users'}{'user_level'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $FormData{'loginname'}, $admin_user_level );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# check whether this person has a valid admin privilege
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database or does not belong to the admin level
		return( 1, 'Please check your login name and try again.' );
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


	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 0, 'The admin user has successfully logged in.' );

}
##### End of sub Process_Admin_Login


##### End of sub routines #####

##### End of script #####
