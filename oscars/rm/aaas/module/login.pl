#!/usr/bin/perl

# login.pl:  login interaction with DB
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### sub Process_User_Login
# In: FormData
# Out: status message
sub Process_User_Login(FormData)
{
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

        $Error_Status = &Database_Lock_Table($db_table_name{'users'});
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
                &Database_Unlock_Table($db_table_name{'users'});
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );


	if ( $Error_Status != 1 )
	{
                &Database_Unlock_Table($db_table_name{'users'});
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# check whether this person is a registered user
	my $Password_Match_Token = 0;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
                &Database_Unlock_Table($db_table_name{'users'});
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
                                &Database_Unlock_Table($db_table_name{'users'});
				return( 0, 'This account is not authorized or activated yet.' );
			}
			elsif ( $$Ref[0] eq &Encode_Passwd( $FormData{'password'} ) )
			{
				$Password_Match_Token = 1;
			}
		}
	}

	&Query_Finish( $Sth );
        &Database_Unlock_Table($db_table_name{'users'});

	if ( !$Password_Match_Token )
	{
		&Database_Disconnect( $Dbh );
		return( 0, 'Please check your password and try again.' );
	}

	### when everything has been processed successfully...
	return( 1, 'The user has successfully logged in.' );

}
##### End of sub Process_User_Login


##### End of sub routines #####

##### End of script #####
