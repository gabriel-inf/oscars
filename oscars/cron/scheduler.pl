#!/usr/bin/perl -w

# Run by cron script

use SOAP::Lite;

use lib qw(/usr/local/esnet/servers/prod);

use strict;

my %params;

my $soap_server = SOAP::Lite
    ->uri('http://localhost:2000/OSCARS/AAAS/Dispatcher')
    ->proxy('http://localhost:2000/aaas');

$params{user_dn} = 'scheduler';
$params{time_interval} = 20;
$params{method} = 'find_pending_reservations';
$params{server_name} = 'BSS';
$params{user_level} = 4;
my $som = $soap_server->dispatch(\%params);
$params{method} = 'find_expired_reservations';
$som = $soap_server->dispatch(\%params);
