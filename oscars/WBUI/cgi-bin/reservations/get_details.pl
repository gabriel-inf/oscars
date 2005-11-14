#!/usr/bin/perl

# details.pl:     Lists the details of a reservation.
# Last modified:  November 8, 2005
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';
require 'print_details.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{server_name} = 'BSS';
$form_params->{method} = 'get_reservation_details';
my $results = get_results($form_params);
if ( !$results ) { exit; }

print_reservation_detail($form_params, $results,
        "Successfully got reservation details",
        $starting_page);
exit;
######

######
1;
