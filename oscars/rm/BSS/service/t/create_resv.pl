#!/usr/bin/perl -w

use DateTime;

require 'soapclient.pl';

my( %params );

    # setup some example stuff
params{'id'} =              'NULL';

    # in seconds since epoch
my $dt = DateTime->now();
$params{'start_time'} =     $dt->epoch + 604800;      # + one week
$params{'end_time'} =       $dt->epoch + 604800*2;    # + two weeks

$params{'created_time'} =   '';   # filled in scheduler
$params{'bandwidth'}=       '50m';
$params{'class'}=           'test';
$params{'burst_limit'}=     '100m';
$params{'status'} =         'pending';

$params{'ingress_interface_id'}= '';   db lookup in scheduler
$params{'egress_interface_id'}=  '';   db lookup in scheduler

    # www.mcs.anl.gov, CGI Perl script will do any DNS lookup
$params{'src_ip'} = '140.221.9.193';     # db lookup in scheduler
    # www.sdsc.edu
$params{'dst_ip'} = '198.202.75.101';    # db lookup in scheduler

$params{'user_dn'} =        'oscars';

$params{'ingress_port'} =   '';     # db lookup in schedule
$params{'egress_port'} =    '';     # db lookup in scheduler

$params{'dscp'} =           '';     # optional

$params{'description'} =    'This is a test.';

my($result, %data) = soap_create_reservation(\%params);
print $data{'status_msg'}, "\n\n";
