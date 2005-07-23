#!/usr/bin/perl -w

use strict;
use DateTime;

use BSS::Client::SOAPClient;

my( %params, $results );

    # in seconds since epoch
my $dt = DateTime->now();
$params{reservation_start_time} =     $dt->epoch - 60;   # - 1 minute
$params{reservation_end_time} =       $dt->epoch + 120;    # + 5 minutes
#$params{reservation_start_time} =     $dt->epoch + 10000;
#$params{reservation_end_time} =       $dt->epoch + 10120;

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

$params{src_address} = 'nettrash3.es.net';
$params{dst_address} = 'atl-pt1.es.net';
#$params{dst_address} = 'snv-rt1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{reservation_description} =    'This is a test.';
$params{method} = 'soap_create_reservation'; 

my $som = bss_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
$results = $som->result;
print STDERR "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results->{reservation_id}.\n";
