#!/usr/bin/perl -w

use SOAP::Lite;

use strict;

use Data::Dumper;

my( %params, @arg_pair );

if ($#ARGV < 0) {
    print STDERR "Usage:  view_details user_level (user or engr)\n";
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

sub usage {
    print STDERR "Invalid command-line arguments:\n";
    print STDERR "To get all reservations:\n";
    print STDERR "\t view_reservations.t engr\n";
    print STDERR "To get all reservations for a specific user:\n";
    print STDERR "\t view_reservations.t user\n";
    exit;
}
