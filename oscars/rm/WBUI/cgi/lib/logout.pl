#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: June 22, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require 'general.pl';


my $cgi = CGI->new();
my ($dn, $user_level) = check_session_status(undef, $cgi);

# nuke session and put the user back at the login screen

if ($dn) { end_session($cgi); }

print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/main_common.js"></script>', "\n";
print '<script language="javascript" type="text/javascript" src="https://oscars.es.net/timeprint.js"></script>', "\n";
print '<script language="javascript">update_status_frame(1, "Please log in");</script>', "\n\n";
print '<script language="javascript">update_main_frame("https://oscars.es.net/login_frame.html");</script>', "\n\n";

exit;

