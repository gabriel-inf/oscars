#!/usr/bin/perl -w

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

$params{'src_hostaddrs_ip'} = '192.168.2.2';
$params{'dst_hostaddrs_ip'} = '192.168.0.2';

$params{'user_dn'} =        'davidr';
$params{'reservation_dscp'} =      'ef';

$params{'reservation_ingress_port'} =   '';     # db lookup in schedule
$params{'reservation_egress_port'} =    '';     # db lookup in scheduler
$params{'reservation_description'} =    'This is a test.';

my($result);
($result, %data) = soap_create_reservation(\%params);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    print $data{'status_msg'}, "\n\n";
}
