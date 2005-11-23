#!/usr/bin/perl -w

use SOAP::Lite;

use strict;

use Data::Dumper;

my( %params );

if ($#ARGV < 0) {
    print STDERR "Usage:  view_details reservation_id\n";
    exit;
}

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
