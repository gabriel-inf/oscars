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
$params{reservation_tag} = $params{user_dn} . '.' . get_time_str($params{reservation_start_time}) . "-";

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



###############################################################################
sub get_time_str {
    my( $epoch_seconds ) = @_;

    my( $second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year, $is_DST);
    ($second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year, $is_DST) = localtime($epoch_seconds); 
    $year += 1900;
    $month += 1;
    if ($month < 10) {
        $month = "0" . $month;
    }
    if ($day < 10) {
        $day = "0" . $day;
    }
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    my $time_tag = $year . $month . $day;

    return( $time_tag );
}
######
