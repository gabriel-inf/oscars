#!/usr/bin/perl

# new_user_form.pl:  Print form for adding a new user
# Last modified:     November 13, 2005
# David Robertson    (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';
require 'print_profile.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"zebratable_ui\">\n";
$form_params->{server_name} = 'AAAS';
$form_params->{method} = 'new_user_form';
print_profile(undef, $form_params, $starting_page);
print  "</div>\n";
print  "</xml>\n";
exit;

######
1;

