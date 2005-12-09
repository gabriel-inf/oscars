#!/usr/bin/perl -w

use strict;
use Test qw(plan ok skip);

plan tests => 6;

use SOAP::Lite;
use Data::Dumper;

my( %params );

$params{reservation_start_time} = time();
$params{duration_hour} =       0.04;    # duration 5 minutes
$params{reservation_time_zone} = "-08:00";

# in Mbps
$params{reservation_bandwidth} =      '10';
$params{reservation_protocol} =       'udp';

$params{source_host} = 'nettrash3.es.net';
$params{destination_host} = 'dc-cr1.es.net';

$params{user_dn} =        'dwrobertson@lbl.gov';
$params{user_level} =        '15';
$params{reservation_description} =    'This is a test.';
$params{server_name} = 'BSS';
$params{method} = 'create_reservation'; 

my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print STDERR Dumper($results);
print STDERR "Your reservation has been processed successfully. Your " .
   "reservation ID number is $results->{reservation_id}.\n";

#############################################################################

my( %params );

$numArgs = $#ARGV + 1;
if ($numArgs < 2) {
    print STDERR "You must provide a reservation id and a user dn\n";
    exit;
}

    # Delete the reservation with the given id (set its status
    # to cancelled).
$params{'reservation_id'} = $ARGV[0];
$params{'user_dn'} = $ARGV[1];

$params{server_name} = 'BSS';
$params{method} = 'delete_reservation';
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
    exit;
}

print STDERR "Successfully cancelled reservation $params{reservation_id}\n";

#############################################################################

my( %params, @arg_pair );

if ($#ARGV < 0) {
    print STDERR "Usage:  view_reservations user_level (user or engr)\n";
    exit;
}

$params{method} = 'view_reservations';
$params{server_name} = 'BSS';
$params{user_dn} = 'dwrobertson@lbl.gov';
if ($ARGV[0] eq 'user') { $params{user_level} = 2; }
elsif ($ARGV[0] eq 'engr') { $params{user_level} = 2 | 4; }
else {
    print STDERR "Invalid argument:  must be user or engr\n";
    exit;
}

my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
my ($r, $f, %c);

for $r (@$results) {
    for $f (keys %$r) {
        if (defined($r->{$f})) {
            print "$f -> $r->{$f}\n";
        }
    }
    print "\n";
}

#############################################################################

my( %params );

$params{reservation_id} = $ARGV[0];
$params{method} = 'view_details';
$params{server_name} = 'BSS';
$params{user_dn} = 'dwrobertson@lbl.gov';
$params{user_level} = 15;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print STDERR Dumper($results);

#############################################################################

my %params;
$params{time_interval} = 20;
$params{method} = 'find_pending_reservations';
$params{server_name} = 'BSS';
$params{user_level} = 4;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
my ($r, $f, %c);

for $r (@$results) {
    for $f (keys %$r) {
        if (defined($r->{$f})) {
            print "$f -> $r->{$f}\n";
        }
    }
    print "\n";
}

#############################################################################

my %params;
$params{time_interval} = 20;
$params{method} = 'find_expired_reservations';
$params{server_name} = 'BSS';
$params{user_level} = 4;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
my ($r, $f, %c);

for $r (@$results) {
    for $f (keys %$r) {
        if (defined($r->{$f})) {
            print "$f -> $r->{$f}\n";
        }
    }
    print "\n";
}
