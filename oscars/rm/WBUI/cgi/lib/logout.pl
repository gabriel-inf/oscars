#!/usr/bin/perl -w

# logout.pl:  Main Service: Logout script
# Last modified: May 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

require 'general.pl';


my $cgi = CGI->new();
my $login_frame;
my ($dn, $user_level, $admin_required) = check_session_status(undef, $cgi);
if (!$admin_required) {
    $login_frame = "https://oscars.es.net/login_frame.html";
}
else {
    $login_frame = "https://oscars.es.net/admin/login_frame.html";
}

# nuke session and put the user back at the login screen

if ($dn) { end_session($cgi); }

update_frames(0, "main_frame", $login_frame, "Please log in.");

exit;

