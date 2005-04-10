package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use AAAS::Frontend::General;
use AAAS::Frontend::Database;

# login:  login interaction with DB

# TODO:  FIX
$non_activated_user_level = -1;

##### sub Process_User_Login
# In: login name, password
# Out: status code, status message
sub Process_User_Login
{
        my($loginname, $password) = @_;
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

        $Error_Code = &Database_Lock_Table($db_table_name{'users'});
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_dn'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
                &Database_Unlock_Table($db_table_name{'users'});
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $loginname );


	if ( $Error_Code )
	{
                &Database_Unlock_Table($db_table_name{'users'});
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# check whether this person is a registered user
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
	        &Query_Finish( $Sth );
                &Database_Unlock_Table($db_table_name{'users'});
		&Database_Disconnect( $Dbh );
		return( 1, 'Please check your login name and try again.' );
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
                                &Database_Unlock_Table($db_table_name{'users'});
				return( 1, 'This account is not authorized or activated yet.' );
			}
			elsif ( $$Ref[0] eq  $password )
			{
				$Password_Match_Token = 1;
			}
                        #print STDERR $$Ref[0] . ' ' . $password;
		}
	}

	&Query_Finish( $Sth );
        &Database_Unlock_Table($db_table_name{'users'});

	if ( !$Password_Match_Token )
	{
		&Database_Disconnect( $Dbh );
		return( 1, 'Please check your password and try again.' );
	}

	### when everything has been processed successfully...
	return( 0, 'The user has successfully logged in.' );

}
##### End of sub Process_User_Login


# logout.pl:  DB operations associated with logout

sub Handle_Logout()
{
my( $Dbh, $Sth, $Error_Code, $Query );

# connect to the database
( $Error_Code, $Dbh ) = &Database_Connect();
if ( $Error_Code )
{
	return( 1, $Error_Code );
}

# TODO:  lock table
# TODO:  redo to change status of user in user table

&Query_Finish( $Sth );

# TODO:  unlock the table(s)

# disconnect from the database
&Database_Disconnect( $Dbh );
}


# myprofile:  Profile DB interaction

##### sub Get_User_Detail
# In: FormData
# Out: user detail, status message
sub Get_User_Detail(FormData)
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

}
##### End of sub Get_User_Detail


##### sub Process_Profile_Update
# In: FormData
# Out: status message
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Profile_Update(FormData)
{
	my( $Dbh, $Sth, $Error_Code, $Query );

	# TODO:  lock necessary tables with LOCK_TABLE

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	###
	# user level provisioning
	# if the user's level equals one of the read-only levels, don't give them access 
	#
	$Query = "SELECT $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

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

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		foreach $ReadOnlyLevel ( @read_only_user_levels )
		{
			if ( $$Ref[0] eq $ReadOnlyLevel )
			{
				&Query_Finish( $Sth );
				&Database_Disconnect( $Dbh );

                                # TODO:  unlock table(s)

				return( 0, '[ERROR] Your user level (Lv. ' . $$Ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.' );
			}
		}
	}

	&Query_Finish( $Sth );

	###
	# read the current user information from the database to decide which fields are being updated
	# 'password' should always be the last entry of the array (important for later procedures)
	#
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

                # TODO:  unlock table(s)
		return( 1, 'Please check the current password and try again.' );
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
                # TODO:  unlock table(s)
		return( 1, 'There is no changed information to update.' );
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

                # TODO:  unlock table(s)
		$Error_Code =~ s/CantExecuteQuery\n//;
		return( 1, 'An error has occurred while updating your account information.<br>[Error] ' . $Error_Code );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

        # TODO:  unlock table(s)

	### when everything has been processed successfully...
	return( 0, 'Your account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update


# activateaccount:  Account Activation DB methods

##### sub Process_User_Account_Activation
# In: FormData
# Out:  StatusMessage 
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Account_Activation(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return (1, $Error_Code );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_activation_key'}, $db_table_field_name{'users'}{'user_pending_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return ( 1, $Error_Code );
	}

	( $Error_Code, $Num_of_Affected_Rows ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return ( 1, $Error_Code );
	}

	# check whether this person is a registered user
	my $Keys_Match_Token = 0;
	my( $Pending_Level, $Non_Match_Error_Message );

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
		&Database_Disconnect( $Dbh );
		return ( 1, 'Please check your login name and try again.' );
	}
	else
	{
		# this login name is in the database; compare passwords
		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			if ( $$Ref[1] eq '' )
			{
				$Non_Match_Error_Message = 'This account has already been activated.';
			}
			elsif ( $$Ref[0] ne &Encode_Passwd( $FormData{'password'} ) )
			{
				$Non_Match_Error_Message = 'Please check your password and try again.';
			}
			elsif ( $$Ref[1] ne $FormData{'activation_key'} )
			{
				$Non_Match_Error_Message = 'Please check the activation key and try again.';
			}
			else
			{
				$Keys_Match_Token = 1;
				$Pending_Level = $$Ref[2];
			}
		}
	}

	&Query_Finish( $Sth );

	### if the input password and the activation key matched against those in the database, activate the account
	if ( $Keys_Match_Token )
	{
                # TODO:  lock necessary tables here
		# change the level to the pending level value and the pending level to 0; empty the activation key field
		$Query = "UPDATE $db_table_name{'users'} SET $db_table_field_name{'users'}{'user_level'} = ?, $db_table_field_name{'users'}{'user_pending_level'} = ?, $db_table_field_name{'users'}{'user_activation_key'} = '' WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

		( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		( $Error_Code, undef ) = &Query_Execute( $Sth, $Pending_Level, '0', $FormData{'loginname'} );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		&Query_Finish( $Sth );

		# TODO:  unlock the table(s)

		# disconnect from the database
		&Database_Disconnect( $Dbh );
	}
	else
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Non_Match_Error_Message );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 0, 'The user account <strong>' . $FormData{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );

}
##### End of sub Process_User_Account_Activation


# register:  user account registration db

##### sub Process_User_Registration
# In: FormData
# Out: status message
sub Process_User_Registration(FormData)
{
	# encrypt password
	my $Encrypted_Password = &Encode_Passwd( $FormData{'password_once'} );

	# get current date/time string in GMT
	my $Current_DateTime = &Create_Time_String( 'dbinput' );

	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query, $Num_of_Affected_Rows );

	# TODO:  lock table(s) with LOCK_TABLES

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	# login name overlap check
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

                # TODO:  unlock table(s)

		return( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
	}

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# initial user level is set to 0; needs admin accept/user activation to raise the user level
	my @Stuffs_to_Insert = ( '', $FormData{'loginname'}, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 0, $Current_DateTime, '', 0 );

	( $Error_Code, undef ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  unlock tables

		$Error_Code =~ s/CantExecuteQuery\n//;
		return( 1, 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Code );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock table(s)

	### when everything has been processed successfully...
	# don't forget to show the user's login name
	return( 0, 'Your user registration has been recorded successfully. Your login name is <strong>' . $FormData{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );

}
##### End of sub Process_User_Registration

1;
