#!/usr/bin/env perl

# activateaccount.pl:  Account Activation page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/database.pl';
require './lib/authenticate.pl';

# template html file names
$interface_template_filename = 'activateaccount.html';

# template html file name for printing process success screen
$processing_result_template_filename = 'accactivated.html';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'login': Process login & forward user to the next appropriate page
# all else (default): Print user login screen
if ( $FormData{'mode'} eq 'activate' )
{
	&Process_User_Account_Activation();
}
else
{
	&Print_Interface_Screen();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{

	my( $Processing_Result, $Processing_Result_Message );
	
	if ( $#_ >= 0 )
	{
		$Processing_Result = $_[0];
		$Processing_Result_Message = $_[1];
	}
	else
	{
		$Processing_Result = 1;
		$Processing_Result_Message = '';
	}

	my $Template_HTML_File;

	if ( ( $Processing_Result == 1 ) && ( $Processing_Result_Message ne '' ) )
	{
		$Template_HTML_File = $processing_result_template_filename;
	}
	else
	{
		$Template_HTML_File = $interface_template_filename;
	}

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach $Html_Line ( @Template_Html )
	{
		foreach ( $Html_Line )
		{
			s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
		}

		print $Html_Line;
	}

	exit;

}
##### sub End of Print_Interface_Screen


##### sub Process_User_Account_Activation
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Account_Activation
{

	# validate user input (just check for empty fields)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter your login name.' );
	}

	if ( $FormData{'activation_key'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the account activation key.' );
	}

	if ( $FormData{'password'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the password.' );
	}

	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# get the password from the database
	$Query = "SELECT $db_table_field_name{'users'}{'user_password'}, $db_table_field_name{'users'}{'user_activation_key'}, $db_table_field_name{'users'}{'user_pending_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# check whether this person is a registered user
	my $Keys_Match_Token = 0;
	my( $Pending_Level, $Non_Match_Error_Message );

	if ( $Num_of_Affected_Rows == 0 )
	{
		# this login name is not in the database
		&Database_Disconnect( $Dbh );
		&Print_Interface_Screen( 0, 'Please check your login name and try again.' );
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
				&Print_Error_Screen( $script_filename, $Error_Status );
			}
		}

		# change the level to the pending level value and the pending level to 0; empty the activation key field
		$Query = "UPDATE $db_table_name{'users'} SET $db_table_field_name{'users'}{'user_level'} = ?, $db_table_field_name{'users'}{'user_pending_level'} = ?, $db_table_field_name{'users'}{'user_activation_key'} = '' WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $Pending_Level, '0', $FormData{'loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
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
		&Print_Interface_Screen( 0, $Non_Match_Error_Message );
	}

	### when everything has been processed successfully...
	# $Processing_Result_Message string may be anything, as long as it's not empty
	&Print_Interface_Screen( 1, 'The user account <strong>' . $FormData{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );

}
##### End of sub Process_User_Account_Activation

##### End of sub routines #####

##### End of script #####
