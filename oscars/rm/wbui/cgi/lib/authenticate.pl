#

# authenticate.pl
#
# library for user authentication
# TODO:  config file
# Last modified: April 8, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### Settings Begin (Global variables) #####

# admin login name (id)
$admin_loginname_string = 'admin';

# admin user level
$admin_user_level = 10;

# non-activated user level (can't login until authorized & activated)
$non_activated_user_level = 0;

# read-only user levels (can make no changes to the database)
@read_only_user_levels = ( '2' );

# user level map & description
# the descriptions will be used for the user list page
# level 0 needs admin authorization to use the system (level upgrade)
# level 10 is reserved for system admin
%user_level_description = (
	0 => 'Authorization Required',
	1 => 'Normal User with All Privileges',
	2 => 'Normal User with Read Privilege Only',
	10 => 'Administrator',
	'pending' => 'Pending User Activation',
);

# user account activation key size (in characters)
$account_activation_key_size = '35';

# admin tool gateway URI (admin login/registration screen)
$admin_tool_gateway_URI = 'https://oscars.es.net/admin/';

# main service login URI (user login screen)
$main_service_login_URI = 'https://oscars.es.net/';

# hardcoded path to the sendmail binary and its flags
# sendmail is not used in this library, but in multiple other places related to user registration
# thus the setting is here
#
# sendmail options:
#    -n  no aliasing
#    -t  read message for "To:"
#    -oi don't terminate message on line containing '.' alone
$sendmail_binary_path_and_flags = '/usr/sbin/sendmail -t -n -oi';

# user account activation form URI
$account_activation_form_URI = 'https://oscars.es.net/user/activateaccount.phtml';

##### Settings End #####


##### End of Library File
# Don't touch the line below
1;
