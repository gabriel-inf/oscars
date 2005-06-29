#!/usr/bin/perl -w

# info_form.pl:  prints information page
# Last modified: June 29, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use CGI;

require 'general.pl';

my( %form_params, $oscars_home);

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}, $oscars_home) =
                                       check_session_status(undef, $cgi);

if ($form_params{user_dn}) {
    print_info($form_params{user_dn}, $form_params{user_level});
    update_status_frame(0, "Information page");
}
else {
    print "Location:  $oscars_home\n\n";
}

exit;

