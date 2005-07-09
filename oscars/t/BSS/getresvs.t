#!/usr/bin/perl -w

use strict;

use BSS::Client::SOAPClient;

use Data::Dumper;

my( %params );
my $numArgs = $#ARGV + 1;

if ($numArgs == 2) {
    if ($ARGV[0] eq '-u') {
        # only get the reservations for that user
        #$params{user_level} = 'engr';
        $params{user_dn} = $ARGV[1];
    }
    else {
        # only get the reservation with that particular key
        $params{user_dn} = 'dwrobertson@lbl.gov';
        $params{reservation_id} = $ARGV[1];
    }
}
else {
    print STDERR "usage:  ./getresvs.pl [-u user_dn | -r reservation_id]\n";
    exit;
}

$params{method} = 'soap_get_reservations';
my $som = bss_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
    exit;
}

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
