#!/usr/bin/env perl

# logout.pl:  Admin tool: Logout Link
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';
require '../lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept GET method only)
# this hash is the only global variable used throughout the script
#%FormData = &Parse_Form_Input_Data( 'get' );

### set cookie with a past expiration date (to delete the cookie that's presently set up)
# retrieve cookie
my %Data_From_Cookie;
@Data_From_Cookie{ 'cookiekey_id', 'user_loginname', 'randomkey' } = &Read_Login_Cookie( $admin_login_cookie_name );

# print Set-Cookie browser header
print &Set_Login_Cookie( 'logout', $admin_login_cookie_name, @Data_From_Cookie{ 'cookiekey_id', 'user_loginname', 'randomkey' } );

### delete this cookie data from the cookiekey table
my( $Dbh, $Sth, $Error_Status, $Query );

# connect to the database
( $Dbh, $Error_Status ) = &Database_Connect();
if ( $Error_Status != 1 )
{
	&Print_Error_Screen( $script_filename, $Error_Status );
}

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

# delete any previously set random keys for the same login name
$Query = "DELETE FROM $db_table_name{'cookiekey'} WHERE $db_table_field_name{'cookiekey'}{'cookiekey_id'} = ?";

( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
if ( $Error_Status != 1 )
{
	&Database_Disconnect( $Dbh );
	&Print_Error_Screen( $script_filename, $Error_Status );
}

( undef, $Error_Status ) = &Query_Execute( $Sth, $Data_From_Cookie{'cookiekey_id'} );
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

# forward the user to the admin tool gateway (login) screen
print "Location: $admin_tool_gateway_URI\n\n";

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### End of sub routines #####

##### End of script #####
