#!/usr/bin/perl

# myprofile.pl:  Profile DB interaction
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of sub routines #####

##### sub Get_User_Detail
# In: FormData
# Out: user detail, status message
sub Get_User_Detail(FormData)
{
		### get the user detail from the database and populate the profile form
		my( $Dbh, $Sth, $Error_Status, $Query );

		# connect to the database
		( $Dbh, $Error_Status ) = &Database_Connect();
		if ( $Error_Status != 1 )
		{
			return( $Error_Status );
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
	my( $Dbh, $Sth, $Error_Status, $Query );

	# TODO:  lock necessary tables with LOCK_TABLE

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	###
	# user level provisioning
	# if the user's level equals one of the read-only levels, don't let them submit a reservation
	#
	$Query = "SELECT $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

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
                # TODO:  unlock table(s)
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

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Values_to_Update, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  unlock table(s)
		$Error_Status =~ s/CantExecuteQuery\n//;
		return( 0, 'An error has occurred while updating your account information.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

        # TODO:  unlock table(s)

	### when everything has been processed successfully...
	return( 1, 'Your account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update

##### End of sub routines #####

##### End of script #####
