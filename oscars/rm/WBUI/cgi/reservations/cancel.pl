#!/usr/bin/perl

# cancel.pl:  Cancel a reservation.
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $auth ) = get_params();
if ( !$form_params ) { exit; }

my $results = get_results($form_params, 'delete_reservation');
if (!$results) { exit; }

print_reservation_detail($form_params,
    $results,
    "Successfully cancelled reservation with id $form_params->{reservation_id}.",
    $auth);
exit;

######
1;
