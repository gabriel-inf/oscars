#!/usr/bin/perl

# cancel.pl:      Cancel a reservation.
# Last modified:  November 13, 2005
# David Robertson (dwrobertson@lbl.gov)

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{server_name} = 'BSS';
$form_params->{method} = 'delete_reservation';
my $results = get_results($form_params);
if (!$results) { exit; }

print_reservation_detail($form_params,
    $results,
    "Successfully cancelled reservation with id $form_params->{reservation_id}.",
    $starting_page);
exit;

######
1;
