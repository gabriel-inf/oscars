#!/usr/bin/perl

# register.pl:  New user account registration
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';
require '../lib/authenticate.pl';	# required for sendmail location

# template html file name for printing browser screen
$interface_template_filename = 'register.html';

# template html file name for printing process success screen
$processing_result_template_filename = 'regprocessed.html';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

# title of the new user registration notification email (sent to the admin)
$registration_notification_email_title = '[Internet2 MPLS Project] New User Registration Notice';

# where to send the notification email
# for multiple addresses, combine them with a comma (e.g. admin1@site.net, admin2@site.net)
$registration_notification_email_toaddr = 'dapi@umich.edu';

# email text encoding (default: ISO-8859-1)
$registration_notification_email_encoding = 'ISO-8859-1';


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# if 'mode' eq 'reserve': Process reservation & print screen with result output (print screen subroutine is called at the end of reservation process)
# all else (default): Print screen for user input
if ( $FormData{'mode'} eq 'register' )
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

##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{

	my( $Processing_Result, $Processing_Result_Message, $Template_HTML_File );
	
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
		&Print_Interface_Screen( 0, 'Please enter your desired login name.' );
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

	# login name overlap check
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

	# insert into database query statement
	$Query = "INSERT INTO $db_table_name{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
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
		&Print_Interface_Screen( 0, 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $Error_Status );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
	}

	### send a notification email to the admin
	open( MAIL, "|$sendmail_binary_path_and_flags $registration_notification_email_toaddr" ) || &Print_Interface_Screen( 0, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

		print MAIL 'From: ', $webmaster, "\n";
		print MAIL 'To: ', $registration_notification_email_toaddr, "\n";

		print MAIL 'Subject: ', $registration_notification_email_title, "\n";
		print MAIL 'Content-Type: text/plain; charset="', $registration_notification_email_encoding, '"', "\n\n";
		
		print MAIL $FormData{'firstname'}, ' ', $FormData{'lastname'}, ' <', $FormData{'email_primary'}, '> has requested a new user account. Please visit the user admin Web page to accept or deny this request.', "\n\n";

		print MAIL 'Login Name: ', $FormData{'loginname'}, "\n\n";

		print MAIL 'Primary E-mail Address: ', $FormData{'email_primary'}, "\n";
		print MAIL 'Secondary E-mail Address: ', $FormData{'email_secondary'}, "\n";
		print MAIL 'Primary Phone Number: ', $FormData{'phone_primary'}, "\n";
		print MAIL 'Secondary Phone Number: ', $FormData{'phone_secondary'}, "\n\n";

		print MAIL '---------------------------------------------------', "\n";
		print MAIL '=== This is an auto-generated e-mail ===', "\n";

	close( MAIL ) || &Print_Interface_Screen( 0, 'Your user registration has been recorded successfully, but sending a notification email to the service administrator has failed. It may take a while longer for the administrator to accept your registration. Please contact the webmaster at ' . $webmaster . ', and inform the person of the date and time of error.<br>[Error] ' . $! );

	### when everything has been processed successfully...
	# don't forget to show the user's login name
	&Print_Interface_Screen( 1, 'Your user registration has been recorded successfully. Your login name is <strong>' . $FormData{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );

}
##### End of sub Process_User_Registration


##### End of sub routines #####

##### End of script #####
