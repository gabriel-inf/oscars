#!/usr/bin/perl

# login.pl:  login interaction with DB
# Last modified: April 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

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


##### End of sub routines #####

##### End of script #####
