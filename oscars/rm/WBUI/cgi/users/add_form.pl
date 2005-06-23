#!/usr/bin/perl

# add_form.pl:  Add a user (requires admin privileges)
# Last modified: June 13, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries

require '../lib/general.pl';


my (%form_params, %results);

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}) = check_session_status(undef, $cgi);

if (!$form_params{user_dn}) {
    print "Location:  https://oscars.es.net/\n\n";
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
update_status_frame(0, "under construction");
exit;
