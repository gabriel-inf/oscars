#!/usr/bin/perl

# adduser.pl:  DB handling for adding a User page
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of sub routines #####

##### sub Check_Loginname_Overlap
# In: FormData
# Out: $Check_Result [yes(overlaps)/no(doesn't overlap)]
sub Check_Loginname_Overlap(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# check whether a particular user id already exists in the database

	# database table & field names to check
	my $Table_Name_to_Check = $db_table_name{'users'};
	my $Field_Name_to_Check = $db_table_field_name{'users'}{'user_loginname'};

	# Query: select user_loginname from users where user_loginname='some_id_to_check';
	$Query = "SELECT $Field_Name_to_Check FROM $Table_Name_to_Check WHERE $Field_Name_to_Check = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'id'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
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

	return $Check_Result;

}
##### End of sub Check_Loginname_Overlap


##### sub Process_User_Registration
# In: FormData
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Registration(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# TODO:  lock  database operations

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# id overlap check
	# we're not using the pre-existing sub routine here to perform the task within a single, locked database connection
	$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

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

	&Query_Finish( $Sth );

	if ( $Num_of_Affected_Rows > 0 )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock

		return( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
	}

	#
	# insert into database query statement
	#

	# initial user level is preset by the admin; no need to wait for authorization
	my @Stuffs_to_Insert = ( '', $FormData{'loginname'}, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 0, $Current_DateTime, $Activation_Key, $FormData{'level'} );

	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( " . join( ', ', ('?') x @Stuffs_to_Insert ) . " )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock
		$Error_Status =~ s/CantExecuteQuery\n//;
		return( 0, 'An error has occurred while recording the account registration on the database.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock the operation
	return( 1, 'The new user account \'' . $FormData{'loginname'} . '\' has been created successfully. <br>The user will receive information on activating the account in email shortly.' );

}
##### End of sub Process_User_Registration

##### End of sub routines #####

##### End of script #####
