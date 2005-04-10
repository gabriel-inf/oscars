#

# sendmail.pl
#
# Configuration: Sendmail settings
# Last modified: March 07, 2005
# Soo-yeon Hwang (dapi@umich.edu)

%cfg_sendmail = (

	# hardcoded path to the sendmail binary and its flags
	#
	# sendmail options:
	#    -n  no aliasing
	#    -t  read message for "To:"
	#    -oi don't terminate message on line containing '.' alone
	binary_path_and_flags => '/usr/sbin/sendmail -t -n -oi',

	# system admin email address
	system_admin_email_address => 'dapi@umich.edu',

	# email text encoding (default: ISO-8859-1)
	email_text_encoding => 'ISO-8859-1',

);

##### End of Configuration File

# Don't touch the line below
1;
