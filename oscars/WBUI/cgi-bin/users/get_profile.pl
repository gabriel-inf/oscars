#!/usr/bin/perl -w

# get_profile.pl: get user's profile
# Last modified:  November 13, 2005
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';
require 'print_profile.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{method} = 'get_profile';
my $results = get_results($form_params);
if (!$results) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"zebratable_ui\">\n";
print_profile($results, $form_params, $starting_page, 'get_profile');
print  "</div>\n";
print  "</xml>\n";
exit;
######

1;
