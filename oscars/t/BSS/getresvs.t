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
        $params{reservation_id} = $ARGV[1];
    }
}
else {
    print STDERR "usage:  ./getresvs.pl [-u user_dn | -r reservation_id]\n";
    exit;
}

my($unused, $results) = soap_get_reservations(\%params);
if (defined($results->{error_msg}) && $results->{error_msg}) {
    print $results->{error_msg}, "\n\n";
}
elsif (defined($results->{status_msg})) {
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
}
