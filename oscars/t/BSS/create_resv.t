#!/usr/bin/perl -w

use strict;
use Data::Dumper;

use BSS::Client::SOAPClient;

my( %params );

$params{reservation_start_time} = time();
$params{duration_hour} =       0.04;    # duration 5 minutes
$params{reservation_time_zone} = "-07:00";

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

$params{source_host} = 'nettrash3.es.net';
$params{destination_host} = 'atl-pt1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{reservation_description} =    'This is a test.';
$params{method} = 'insert_reservation'; 

my $som = bss_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
my $rows = $results->{rows};
my $r;
for $r (@$rows) {
    print STDERR "Your reservation has been processed " .
        "successfully. Your reservation ID number is $r->{reservation_id}.\n";
}
