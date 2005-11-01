#!/usr/bin/perl -w

# info_form.pl:  prints information page
# Last modified: October 31, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;

require 'general.pl';

my $cgi = CGI->new();
my ($user_dn, $user_level, $unused, $starting_page) = verify_session($cgi);

if ($user_dn) {
    print $cgi->header( -type=>'text/xml' );
    update_page("Information page.", \&output_info, $user_dn, $user_level);
}
else {
    print "Location:  $starting_page\n\n";
}

exit;
