#!/usr/bin/perl -w

# Run by cron script

use SOAP::Lite;

use strict;

my %params;

my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

$params{time_interval} = 20;
$params{method} = 'find_pending_reservations';
$params{server_name} = 'BSS';
$params{user_level} = 4;
my $som = $soap_server->dispatch(\%params);
$params{method} = 'find_expired_reservations';
$som = $soap_server->dispatch(\%params);
