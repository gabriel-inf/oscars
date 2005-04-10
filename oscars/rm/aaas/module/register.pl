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


##### End of sub routines #####

##### End of script #####
