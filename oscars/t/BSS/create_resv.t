#!/usr/bin/perl -w

use strict;
use DateTime;
use Data::Dumper;

use BSS::Client::SOAPClient;

my( %params, $results );

    # in seconds since epoch
my $dt = DateTime->now();
$params{reservation_start_time} =     $dt->epoch - 60;   # - 1 minute
my($sec, $min, $hour, $mday, $month, $year, $wday, $yday, $dst) =
    localtime(time());
$year += 1900;
$month += 1;
if ($month < 10) { $month = sprintf("0%s", $month); }
if ($mday < 10) { $mday = sprintf("0%s", $mday); }
if ($hour < 10) { $hour = sprintf("0%s", $hour); }
if ($min < 10) { $min = sprintf("0%s", $min); }
$params{reservation_start_time} = $year . '-' . $month . '-' . $mday . ' ' . $hour . ':' . $min . ":00";
print $params{reservation_start_time}, "\n";
$params{duration_hour} =       0.04;    # duration 5 minutes
$params{timezone_offset} = "-07:00";

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

$params{src_address} = 'nettrash3.es.net';
$params{dst_address} = 'atl-pt1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{reservation_description} =    'This is a test.';
$params{method} = 'insert_reservation'; 

print STDERR Dumper(%params);
my $som = bss_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
$results = $som->result;
print STDERR "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results->{reservation_id}.\n";
