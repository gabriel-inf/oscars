#!/usr/bin/perl -w

use SOAP::Lite;

use strict;

use Data::Dumper;

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
