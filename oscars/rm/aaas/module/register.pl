#!/usr/bin/perl

# register.pl:  user account registration db script
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';


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
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			return( $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	# login name overlap check
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

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		return( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
	}

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	# initial user level is set to 0; needs admin accept/user activation to raise the user level
	my @Stuffs_to_Insert = ( '', $FormData{'loginname'}, $Encrypted_Password, $FormData{'firstname'}, $FormData{'lastname'}, $FormData{'organization'}, $FormData{'email_primary'}, $FormData{'email_secondary'}, $FormData{'phone_primary'}, $FormData{'phone_secondary'}, $FormData{'description'}, 0, $Current_DateTime, '', 0 );

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		return( 0, 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
	}

	### when everything has been processed successfully...
	# don't forget to show the user's login name
	return( 1, 'Your user registration has been recorded successfully. Your login name is <strong>' . $FormData{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );

}
##### End of sub Process_User_Registration


##### End of sub routines #####

##### End of script #####
