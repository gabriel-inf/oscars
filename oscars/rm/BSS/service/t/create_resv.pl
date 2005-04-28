#!/usr/bin/perl -w

use DateTime;

use lib '../../..';

use BSS::Client::SOAPClient;

my( %params );

    # setup some example stuff
$params{'id'} =              'NULL';

    # in seconds since epoch
my $dt = DateTime->now();
$params{'start_time'} =     $dt->epoch - 1200;   # - 20 minutes
$params{'end_time'} =       $dt->epoch + 120;    # + 2 minutes

$params{'created_time'} =   '';   # filled in scheduler
$params{'bandwidth'}=       '50m';
$params{'class'}=           'test';
$params{'burst_limit'}=     '100m';
$params{'status'} =         'pending';

$params{'ingress_interface_id'}= '';   # db lookup in scheduler
$params{'egress_interface_id'}=  '';   # db lookup in scheduler

$params{'src_ip'} = '192.168.2.2';
$params{'dst_ip'} = '192.168.0.2';

$params{'dn'} =        'oscars';

$params{'ingress_port'} =   '';     # db lookup in schedule
$params{'egress_port'} =    '';     # db lookup in scheduler

$params{'dscp'} =           '';     # optional

$params{'description'} =    'This is a test.';

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
