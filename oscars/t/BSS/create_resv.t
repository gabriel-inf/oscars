#!/usr/bin/perl -w

use strict;
use DateTime;

use BSS::Client::SOAPClient;

my( %params );

    # in seconds since epoch
my $dt = DateTime->now();
$params{reservation_start_time} =     $dt->epoch - 60;   # - 1 minute
$params{reservation_end_time} =       $dt->epoch + 120;    # + 5 minutes

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

#$params{src_address} = '134.79.240.36';
#$params{dst_address} = '192.91.245.29';
$params{src_address} = 'nettrash3.es.net';
$params{dst_address} = 'atl-pt1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{reservation_description} =    'This is a test.';
$params{method} = 'soap_create_reservation'; 

my $som = bss_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
    exit;
}

print STDERR "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results->{reservation_id}.\n";
