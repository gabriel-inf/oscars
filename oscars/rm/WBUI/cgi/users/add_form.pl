#!/usr/bin/perl

# add_form.pl:  Admin tool: Add a User page
# Last modified: June 7, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries

require '../lib/general.pl';


my (%form_params, %results);

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}, $form_params{form_type}) = check_session_status(undef, $cgi);

if ($form_params{user_dn}) {
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    update_frames(0, "status_frame", "", "under construction");
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;
