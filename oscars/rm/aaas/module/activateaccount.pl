#!/usr/bin/perl

# activateaccount.pl:  Account Activation DB script
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'lib/general.pl';
require 'database.pl';

##### sub Process_User_Account_Activation
# In: FormData
# Out:  StatusMessage 
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Account_Activation(FormData)
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return ($Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_activation_key'}, $db_table_field_name{'users'}{'user_pending_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return ( $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return ( $Error_Status );
	}

	# check whether this person is a registered user
	my $Keys_Match_Token = 0;
	my( $Pending_Level, $Non_Match_Error_Message );

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
		&Database_Disconnect( $Dbh );
		return ( 0, 'Please check your login name and try again.' );
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

		# change the level to the pending level value and the pending level to 0; empty the activation key field
		$Query = "UPDATE $db_table_name{'users'} SET $db_table_field_name{'users'}{'user_level'} = ?, $db_table_field_name{'users'}{'user_pending_level'} = ?, $db_table_field_name{'users'}{'user_activation_key'} = '' WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $Pending_Level, '0', $FormData{'loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		&Query_Finish( $Sth );

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
		return( 0, $Non_Match_Error_Message );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	return( 1, 'The user account <strong>' . $FormData{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );

}
##### End of sub Process_User_Account_Activation

##### End of sub routines #####

##### End of script #####
