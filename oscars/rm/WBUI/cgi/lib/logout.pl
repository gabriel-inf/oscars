#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: June 6, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require 'general.pl';


my $cgi = CGI->new();
my $login_frame;
my ($dn, $user_level) = check_session_status(undef, $cgi);

$login_frame = "https://oscars.es.net/login_frame.html";

# nuke session and put the user back at the login screen

if ($dn) { end_session($cgi); }

update_frames(0, "success", "main_frame", $login_frame, "Please log in.");

exit;

