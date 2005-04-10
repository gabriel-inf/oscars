#!/usr/bin/perl

# logout.pl:  Admin tool: Logout script
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# Receive data from HTML form (accept GET method only)
# this hash is the only global variable used throughout the script
#%FormData = &Parse_Form_Input_Data( 'get' );

#  TODO:  connect to AAAS, perform cleanup

# forward the user to the admin tool gateway (login) screen
print "Location: $admin_tool_gateway_URI\n\n";

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### End of sub routines #####

##### End of script #####
