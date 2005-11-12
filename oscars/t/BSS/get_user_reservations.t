#!/usr/bin/perl -w

use strict;

use SOAP::Lite;

use Data::Dumper;

my( %params );
my $numArgs = $#ARGV + 1;

if ($numArgs == 1) {
    $params{user_dn} = $ARGV[0];
}
else {
    print STDERR "usage:  ./get_user_reservations.pl user_dn\n";
    exit;
}

$params{method} = 'get_user_reservations';
$params{server_name} = 'BSS';
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;

my ($rows, $r, $f, %c);

$rows = $results->{rows};
for $r (@$rows) {
    for $f (keys %$r) {
        if (defined($r->{$f})) {
            print "$f -> $r->{$f}\n";
        }
    }
    print "\n";
}
