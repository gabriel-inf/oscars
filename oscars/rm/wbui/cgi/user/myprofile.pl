#!/usr/bin/perl

# myprofile.pl:  Main service: My Profile page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/database.pl';
require './lib/authenticate.pl';

# template html file names
$interface_template_filename = 'myprofile.html';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# check if the user is logged in
if ( &Verify_Login_Status( $user_login_cookie_name ) != 1 )
{
	# forward the user to the user login screen
	print "Location: $main_service_login_URI\n\n";
	exit;
}
else
{
	$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];
}

# if 'mode' eq 'updatemyprofile': Update the currently logged-in user's profile
# all else (default): Print the My Profile interface page
if ( $FormData{'mode'} eq 'updatemyprofile' )
{
	&Process_Profile_Update();
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
	
	# open html template file
	open( F_HANDLE, $interface_template_filename ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $interface_template_filename . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	# join all the html lines into one, to make the regex work easier
	my $Html_Line = join( '', @Template_Html );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach ( $Html_Line )
	{
		s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
		s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'loginname'}/g;
	}

	# if processing has failed for some reason, pre-fill the form with the %FormData values so that users do not need to fill the form again
	if ( $Processing_Result == 0 )
	{
		foreach $Key ( keys %FormData )
		{
			foreach ( $Html_Line )
			{
				s/(name="$Key")/$1 value="$FormData{$Key}"/ig;
			}
		}
	}
	else
	{
		### get the user detail from the database and populate the profile form
		my( $Dbh, $Sth, $Error_Status, $Query );

		# connect to the database
		( $Dbh, $Error_Status ) = &Database_Connect();
		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
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
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_Error_Screen( $script_filename, $Error_Status );
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
	}

	print $Html_Line;

	exit;

}
##### End of sub Print_Interface_Screen


##### sub Process_Profile_Update
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Profile_Update
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'password_current'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the current password.' );
	}

	my $Encrypted_Password;
	my $Update_Password = 0;

	if ( $FormData{'password_new_once'} ne '' || $FormData{'password_new_twice'} ne '' )
	{
		if ( $FormData{'password_new_once'} ne $FormData{'password_new_twice'} )
		{
			&Print_Interface_Screen( 0, 'Please enter the same new password twice for verification.' );
		}
		else
		{
			# encrypt the new password
			$Encrypted_Password = &Encode_Passwd( $FormData{'password_new_once'} );

			$Update_Password = 1;
		}
	}

	### begin working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		foreach $ReadOnlyLevel ( @read_only_user_levels )
		{
			if ( $$Ref[0] eq $ReadOnlyLevel )
			{
				&Query_Finish( $Sth );
				&Database_Disconnect( $Dbh );

				if ( $use_lock ne 'off' )
				{
					&Lock_Release();
				}

				&Print_Interface_Screen( 0, '[ERROR] Your user level (Lv. ' . $$Ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.' );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
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

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		&Print_Interface_Screen( 0, 'Please check the current password and try again.' );
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

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		&Print_Interface_Screen( 0, 'There is no changed information to update.' );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Values_to_Update, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_Interface_Screen( 0, 'An error has occurred while updating your account information.<br>[Error] ' . $Error_Status );
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
	&Print_Interface_Screen( 1, 'Your account information has been updated successfully.' );

}
##### End of sub Process_Profile_Update

##### End of sub routines #####

##### End of script #####
