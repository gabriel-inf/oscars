#!/usr/bin/perl

# details.pl:  Lists the details of a reservation.
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Data::Dumper;

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $auth, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

my $results = get_results($form_params, 'get_reservations');
if ( !$results ) { exit; }

print_reservation_detail($form_params, $results,
        "Successfully got reservation details",
        $auth, $starting_page);
exit;
######

######
1;
