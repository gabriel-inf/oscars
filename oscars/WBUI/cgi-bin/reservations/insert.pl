#!/usr/bin/perl

# insert.pl:  Insert a reservation to be scheduled.
# Last modified: October 31, 2005
# David Robertson (dwrobertson@lbl.gov)

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{method} = 'insert_reservation';
my $results = get_results($form_params);
if (!$results) { exit; }

$form_params->{reservation_id} = $results->{reservation_id};
print_reservation_detail($form_params, $results,
    "Successfully created reservation with id $results->{reservation_id}.",
    $starting_page);
exit;
######

######
1;
