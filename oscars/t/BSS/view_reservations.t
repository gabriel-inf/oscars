#!/usr/bin/perl -w

use SOAP::Lite;

use strict;

use Data::Dumper;

my( %params );
my $numArgs = $#ARGV + 1;

if ($numArgs == 1) {
    $params{sql_filter} = $ARGV[0];
}
else {
    print "usage:  ./get_user_reservations.pl sql_filter\n";
    print "example SQL filters (used in a statement after a WHERE):\n\n";

    print "all\n";
    print "\tgets all reservations you have permissions for\n\n";

    print "user_dn = 'dwrobertson\@lbl.gov'\n";
    print "\tgets all reservations for a particular user\n\n";

    print "reservation_id = 14\n";
    print "\tgets the details for a particular reservation\n";
    exit;
}

$params{method} = 'view_reservations';
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
my ($r, $f, %c);

for $r (@$results) {
    for $f (keys %$r) {
        if (defined($r->{$f})) {
            print "$f -> $r->{$f}\n";
        }
    }
    print "\n";
}
