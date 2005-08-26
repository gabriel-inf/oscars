#!/usr/bin/perl

# insert.pl:  Insert a reservation to be scheduled.
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $auth ) = get_params();
if ( !$form_params ) { exit; }

my $results = get_results($form_params, 'insert_reservation');
if (!$results) { exit; }

$form_params->{reservation_id} = $results->{reservation_id};
print_reservation_detail($form_params, $results,
    "Successfully created reservation with id $results->{reservation_id}.",
    $auth);
exit;
######

######
1;
