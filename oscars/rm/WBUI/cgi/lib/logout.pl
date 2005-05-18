#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: May 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require 'general.pl';


my $cgi = CGI->new();
my ($dn, $user_level, $admin_required) = check_session_status(undef, $cgi);

# nuke session and put the user back at the login screen

if ($dn) { end_session($cgi); }

update_frames(0, "main_frame", "https://oscars.es.net/login_frame.html", "Please log in.");

exit;

