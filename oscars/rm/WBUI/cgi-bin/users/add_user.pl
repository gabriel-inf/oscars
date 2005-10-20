#!/usr/bin/perl

# add_user.pl:  Add a user (requires admin privileges)
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Data::Dumper;

require '../lib/general.pl';
require 'print_profile.pl';

my( $form_params, $auth, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{method} = 'add_user';
my $results = get_results($form_params);
if (!$results) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"zebratable_ui\">\n";
print_profile($results, $form_params, $starting_page);
print  "</div>\n";
print  "</xml>\n";
exit;

######
1;

