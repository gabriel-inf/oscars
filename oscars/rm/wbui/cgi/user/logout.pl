#!/usr/bin/perl

# logout.pl:  Main Service: Logout Link
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept GET method only)
# this hash is the only global variable used throughout the script
#%FormData = &Parse_Form_Input_Data( 'get' );

### set cookie with a past expiration date (to delete the cookie that's presently set up)
# retrieve cookie
my %Data_From_Cookie;
@Data_From_Cookie{ 'cookiekey_id', 'user_loginname', 'randomkey' } = &Read_Login_Cookie( $user_login_cookie_name );

# print Set-Cookie browser header
print &Set_Login_Cookie( 'logout', $user_login_cookie_name, @Data_From_Cookie{ 'cookiekey_id', 'user_loginname', 'randomkey' } );

# TODO:  connect to the database, return status

# forward the user to the admin tool gateway (login) screen
# TODO:  set location
print "Location: $main_service_login_URI\n\n";

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### End of sub routines #####

##### End of script #####
