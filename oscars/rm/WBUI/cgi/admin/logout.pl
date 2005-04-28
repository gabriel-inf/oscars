#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout Link
# Last modified: April 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require '../lib/general.pl';

# Currently (4-27-05) same as ../user/logout.pl



my $cgi = CGI->new();
my $error_status = check_login(0, $cgi);

# nuke session and put the user back at the login screen

if (!$error_status) {
    end_session($cgi);
}

print "Content-type: text/html\n\n";

update_frames("main_frame", "https://oscars.es.net/login_frame.html", "Please log in.");

exit;

