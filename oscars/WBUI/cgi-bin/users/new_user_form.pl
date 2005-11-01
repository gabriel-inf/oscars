#!/usr/bin/perl

# new_user_form.pl:  Print form for adding a new user
# Last modified: October 31, 2005
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';
require 'print_profile.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"zebratable_ui\">\n";
if (authorized($form_params->{user_level}, "admin")) {
    $form_params->{admin_dn} = $form_params->{user_dn};
    $form_params->{method} = 'new_user_form';
    print_profile(undef, $form_params, $starting_page);
}
print  "</div>\n";
print  "</xml>\n";
exit;

######
1;

