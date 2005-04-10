package AAAS::Frontend::Admin;

# Admin.pm:  database operations associated with administrative forms

# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use AAAS::Frontend::General;
use AAAS::Frontend::Database;


# gateway:  DB operations having to do with logging in as admin

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


# adduser:  DB handling for adding a User page

##### sub Check_Loginname_Overlap
# In: FormData
# Out: $Check_Result [yes(overlaps)/no(doesn't overlap)]
sub Check_Loginname_Overlap(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# check whether a particular user id already exists in the database

	# database table & field names to check
	my $Table_Name_to_Check = $db_table_name{'users'};
	my $Field_Name_to_Check = $db_table_field_name{'users'}{'user_loginname'};

	# Query: select user_loginname from users where user_loginname='some_id_to_check';
	$Query = "SELECT $Field_Name_to_Check FROM $Table_Name_to_Check WHERE $Field_Name_to_Check = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $FormData{'id'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	my $Check_Result;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# ID does not overlap; usable
		$Check_Result = 'no';
	}
	else
	{
		# ID is already taken by someone else; unusable
		$Check_Result = 'yes';
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	return (0, $Check_Result);

}
##### End of sub Check_Loginname_Overlap


##### sub Process_User_Registration
# In: FormData
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Registration(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	# TODO:  lock  database operations

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# id overlap check
	# we're not using the pre-existing sub routine here to perform the task within a single, locked database connection
	$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	&Query_Finish( $Sth );

	if ( $Num_of_Affected_Rows > 0 )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock

		return( 1, 'The selected login name is already taken by someone else; please choose a different login name.' );
	}

	#
	# insert into database query statement
	#

	# initial user level is preset by the admin; no need to wait for authorization
	my @Stuffs_to_Insert = ( '', $FormData{'loginname'}, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 0, $Current_DateTime, $Activation_Key, $FormData{'level'} );

	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( " . join( ', ', ('?') x @Stuffs_to_Insert ) . " )";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( undef, $Error_Code ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock
		$Error_Code =~ s/CantExecuteQuery\n//;
		return( 1, 'An error has occurred while recording the account registration on the database.<br>[Error] ' . $Error_Code );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock the operation
	return( 0, 'The new user account \'' . $FormData{'loginname'} . '\' has been created successfully. <br>The user will receive information on activating the account in email shortly.' );

}
##### End of sub Process_User_Registration


# editprofile:  DB handling for Edit Admin Profile page

##### sub Get_User_Profile
# In:  FormData
# Out: status message and DB results
sub Get_User_Profile
{
		### get the user detail from the database and populate the profile form
		my( $Dbh, $Sth, $Error_Code, $Query );

		# connect to the database
		( $Error_Code, $Dbh ) = &Database_Connect();
		if ( $Error_Code )
		{
			return( 1, $Error_Code );
		}

		# names of the fields to be displayed on the screen
		my @Fields_to_Display = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description' );

		# DB Query: get the user profile detail
		$Query = "SELECT ";
		foreach $_ ( @Fields_to_Display )
		{
			my $Temp = 'user_' . $_;
			$Query .= $db_table_field_name{'users'}{$Temp} . ", ";
		}
		# delete the last ", "
		$Query =~ s/,\s$//;
		$Query .= " FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

		( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		( $Error_Code, undef ) = &Query_Execute( $Sth, $FormData{'loginname'} );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		# populate %User_Profile_Data with the data fetched from the database
		my %User_Profile_Data;
		@User_Profile_Data{@Fields_to_Display} = ();
		$Sth->bind_columns( map { \$User_Profile_Data{$_} } @Fields_to_Display );
		$Sth->fetch();

		&Query_Finish( $Sth );

		# disconnect from the database
		&Database_Disconnect( $Dbh );

		foreach $Field ( @Fields_to_Display )
		{
			$Html_Line =~ s/(name="$Field")/$1 value="$User_Profile_Data{$Field}"/i;
		}
    # TODO:  return user profile to CGI script

}
##### End of sub Print_Interface_Screen


##### sub Process_Profile_Update
# In: FormData
# Out: status message, results
sub Process_Profile_Update
{
	### begin working with the database
	my( $Dbh, $Sth, $Error_Code, $Query );

	# TODO:  lock with LOCK_TABLE

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# read the current user information from the database to decide which fields are being updated
	# 'password' should always be the last entry of the array (important for later procedures)
	my @Fields_to_Read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'password' );

	# DB Query: get the user profile detail
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		my $Temp = 'user_' . $_;
		$Query .= $db_table_field_name{'users'}{$Temp} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	$Query .= " FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, undef ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# populate %User_Profile_Data with the data fetched from the database
	my %User_Profile_Data;
	@User_Profile_Data{@Fields_to_Read} = ();
	$Sth->bind_columns( map { \$User_Profile_Data{$_} } @Fields_to_Read );
	$Sth->fetch();

	&Query_Finish( $Sth );

	### check the current password with the one in the database before proceeding
	if ( $User_Profile_Data{'password'} ne &Encode_Passwd( $FormData{'password_current'} ) )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock
		return( 0, 'Please check the current password and try again.' );
	}

	### update information in the database

	# determine which fields to update in the user profile table
	# @Fields_to_Update and @Values_to_Update should be an exact match
	my( @Fields_to_Update, @Values_to_Update );

	# if the password needs to be updated, add the new one to the fields/values to update
	if ( $Update_Password )
	{
		push( @Fields_to_Update, $db_table_field_name{'users'}{'user_password'} );
		push( @Values_to_Update, $Encrypted_Password );
	}

	# remove password from the update comparison list
	# 'password' is the last element of the array; remove it from the array
	$#Fields_to_Read--;

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

                # TODO:  release lock
		return( 0, 'There is no changed information to update.' );
	}

	# prepare the query for database update
	$Query = "UPDATE $db_table_name{'users'} SET ";
	foreach $_ ( 0 .. $#Fields_to_Update )
	{
		$Query .= $Fields_to_Update[$_] . " = ?, ";
	}
	$Query =~ s/,\s$//;
	$Query .= " WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, undef ) = &Query_Execute( $Sth, @Values_to_Update, $FormData{'loginname'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock
		$Error_Code =~ s/CantExecuteQuery\n//;
		return( 1, 'An error has occurred while updating the account information.<br>[Error] ' . $Error_Code );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock table

	### when everything has been processed successfully...
	return( 0, 'The account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update


# logout:   DB operations associated with admin logout

sub Handle_Admin_Logout(FormData)
{
    # connect to the database
    ( $Error_Code, $Dbh ) = &Database_Connect();
    if ( $Error_Code )
    {
        return( 1, $Error_Code );
    }

    # TODO:  lock table

    &Query_Finish( $Sth );

    # TODO:  unlock the operation

    # disconnect from the database
    &Database_Disconnect( $Dbh );

    # TODO:  return status
    return ( 1, 'not done yet' );
}

1;
