#!/usr/bin/perl -w

# Run by cron script

use SOAP::Lite;

use strict;

my %params;

my $soap_server = SOAP::Lite
    ->uri('http://localhost:2000/OSCARS/Dispatcher')
    ->proxy('http://localhost:2000/Server');

$params{user_dn} = 'scheduler';
$params{time_interval} = 20;
$params{method} = 'FindPendingReservations';
$params{server_name} = 'BSS';
$params{user_level} = 4;
my $som = $soap_server->dispatch(\%params);
$params{method} = 'FindExpiredReservations';
$som = $soap_server->dispatch(\%params);
