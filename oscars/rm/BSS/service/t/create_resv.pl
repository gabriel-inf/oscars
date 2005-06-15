#!/usr/bin/perl -w

use strict;

use DateTime;

use BSS::Client::SOAPClient;

my( %params );

    # setup some example stuff
$params{'reservation_id'} =              'NULL';

    # in seconds since epoch
my $dt = DateTime->now();
$params{'reservation_start_time'} =     $dt->epoch - 60;   # - 1 minute
$params{'reservation_end_time'} =       $dt->epoch + 120;    # + 5 minutes

$params{'reservation_created_time'} =   '';   # filled in scheduler
$params{'reservation_bandwidth'} =      '10m';
$params{'reservation_class'} =          '4';
$params{'reservation_burst_limit'} =    '1m';
$params{'reservation_status'} =         'pending';

$params{'ingress_interface_id'}= '';   # db lookup in scheduler
$params{'egress_interface_id'}=  '';   # db lookup in scheduler

$params{'src_hostaddrs_ip'} = 'nettrash3.es.net';
$params{'dst_hostaddrs_ip'} = 'atl-pt1.es.net';

$params{'user_dn'} =        'dwrobertson@lbl.gov';
$params{'reservation_description'} =    'This is a test.';

my($status, $results);
($status, $results) = soap_create_reservation(\%params);
if (defined($results->{'error_msg'}) && $results->{'error_msg'})
{
    print $results->{'error_msg'}, "\n\n";
}
elsif (defined($results->{'status_msg'}))
{
    print $results->{'status_msg'}, "\n\n";
}
