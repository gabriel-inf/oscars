#!/usr/bin/env perl

# adduser.pl:  Admin tool: Add a User page
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';
require '../lib/authenticate.pl';

# template html file names
%interface_template_filename = (
	'adminadduser' => 'adduser.html',
	'idcheck' => 'idcheck.html'
);

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

# smiley icons used in the login name overlap check result page
%icon_locations = (
	'smile' => '../images/icon_biggrin.gif',	# smile face
	'sad' => '../images/icon_sad.gif',		# sad face
	'exclaim' => '../images/icon_exclaim.gif'	# "!" mark
);

# title of the user account authorization notification email (sent to the user)
$adminadduser_notification_email_title = '[Internet2 MPLS Project] A new user account has been created for you';

# email text encoding (default: ISO-8859-1)
$adminadduser_notification_email_encoding = 'ISO-8859-1';


##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

# check if the user is logged in
if ( &Verify_Login_Status( $admin_login_cookie_name ) != 1 )
{
	# forward the user to the admin tool gateway (login) screen
	print "Location: $admin_tool_gateway_URI\n\n";
	exit;
}
else
{
	$FormData{'current_loggedin_name'} = ( &Read_Login_Cookie( $admin_login_cookie_name ) )[1];
}

# if 'mode' eq 'idcheck': Check the input login name for overlap
# if 'mode' eq 'adminadduser': Add a new user (pass the authorization step) and send an activation info email to the user
# all else (default): Print the new user registration page
if ( $FormData{'mode'} eq 'idcheck' )
{
	&Print_Loginname_Overlap_Check_Result();
}
elsif ( $FormData{'mode'} eq 'adminadduser' )
{
	&Process_User_Registration();
}
else
{
	&Print_Interface_Screen();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Loginname_Overlap_Check_Result
# In: None
# Out: None (exits the program at the end)
sub Print_Loginname_Overlap_Check_Result
{

	my $Processing_Result_Message = '<strong>' . $FormData{'id'} . '</strong><br>';

	if ( $FormData{'id'} =~ /\W|\s/)
	{
		$Processing_Result_Message .= '<img src="' . $icon_locations{'exclaim'} . '" alt="!"> Please use only alphanumeric characters or _ for login name.';
	}
	else
	{
		my $Overlap_Check_Result = &Check_Loginname_Overlap();

		if ( $Overlap_Check_Result eq 'no' )
		{
			$Processing_Result_Message .= '<img src="' . $icon_locations{'smile'} . '" alt="smile face"> You can use this login ID.';
		}
		elsif ( $Overlap_Check_Result eq 'yes' )
		{
			$Processing_Result_Message .= '<img src="' . $icon_locations{'sad'} . '" alt="sad face"> This login name is already taken; please choose something else.';
		}
	}

	my $Template_HTML_File = $interface_template_filename{'idcheck'};

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	# print processing result to browser screen
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
##### End of sub Print_Loginname_Overlap_Check_Result


##### sub Check_Loginname_Overlap
# In: None
# Out: $Check_Result [yes(overlaps)/no(doesn't overlap)]
sub Check_Loginname_Overlap
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'id'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
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
	
	my $Template_HTML_File = $interface_template_filename{'adminadduser'};

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
			s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'current_loggedin_name'}/g;
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

		print $Html_Line;
	}

	exit;

}
##### End of sub Print_Interface_Screen


##### sub Process_User_Registration
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_User_Registration
{

	# validate user input (fairly minimal... Javascript also takes care of form data validation)
	if ( $FormData{'loginname'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the desired login name.' );
	}
	elsif ( $FormData{'loginname'} =~ /\W|\s/ )
	{
		&Print_Interface_Screen( 0, 'Please use only alphanumeric characters or _ for login name.' );
	}

	if ( $FormData{'password_once'} eq '' || $FormData{'password_twice'} eq '' )
	{
		&Print_Interface_Screen( 0, 'Please enter the password.' );
	}
	elsif ( $FormData{'password_once'} ne $FormData{'password_twice'} )
	{
		&Print_Interface_Screen( 0, 'Please enter the same password twice for verification.' );
	}

	# encrypt password
	my $Encrypted_Password = &Encode_Passwd( $FormData{'password_once'} );

	# get current date/time string in GMT
	my $Current_DateTime = &Create_Time_String( 'dbinput' );

	# create user account activation key
	my $Activation_Key = &Generate_Random_String( $account_activation_key_size );

	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

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

	# id overlap check
	# we're not using the pre-existing sub routine here to perform the task within a single, locked database connection
	$Query = "SELECT $db_table_field_name{'users'}{'user_loginname'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

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

	&Query_Finish( $Sth );

	if ( $Num_of_Affected_Rows > 0 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		&Print_Interface_Screen( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		$Error_Status =~ s/CantExecuteQuery\n//;
		&Print_Interface_Screen( 0, 'An error has occurred while recording the account registration on the database.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
	}

	### send a notification email to the user (with account activation instruction)
	open( MAIL, "|$sendmail_binary_path_and_flags $FormData{'email_primary'}" ) || &Print_Interface_Screen( 0, 'The user account has been created successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );

		print MAIL 'From: ', $webmaster, "\n";
		print MAIL 'To: ', $FormData{'email_primary'}, "\n";

		print MAIL 'Subject: ', $adminadduser_notification_email_title, "\n";
		print MAIL 'Content-Type: text/plain; charset="', $adminadduser_notification_email_encoding, '"', "\n\n";
		
		print MAIL 'A new user account has been created for you, and is ready for activation.', "\n";
		print MAIL 'Please visit the Web page below and activate your user account. If the URL appears in multiple lines, please copy and paste the whole address on your Web browser\'s address bar.', "\n\n";

		print MAIL $account_activation_form_URI, "\n\n";

		print MAIL 'Your Login Name: ', $FormData{'loginname'}, "\n";
		print MAIL 'Account Activation Key: ', $Activation_Key, "\n";
		print MAIL 'Password: ', $FormData{'password_once'}, "\n";
		print MAIL 'Your User Level: ', $user_level_description{$FormData{'level'}}, ' (Lv ', $FormData{'level'}, ')', "\n";
		print MAIL 'Please change the password to your own once you activate the account.', "\n\n";

		print MAIL '---------------------------------------------------', "\n";
		print MAIL '=== This is an auto-generated e-mail ===', "\n";

	close( MAIL ) || &Print_Interface_Screen( 0, 'The user account has been created successfully, but sending a notification email to the user has failed. If the user does not receive the activation key by email, the account cannot be activated. Please check all the settings and the user\'s primary email address, and contact the webmaster at ' . $webmaster . ' and inform the person of the date and time of error.<br>[Error] ' . $! );

	### when everything has been processed successfully...
	# don't forget to show the user's login name
	&Print_Interface_Screen( 1, 'The new user account \'' . $FormData{'loginname'} . '\' has been created successfully. <br>The user will receive information on activating the account in email shortly.' );

}
##### End of sub Process_User_Registration

##### End of sub routines #####

##### End of script #####
