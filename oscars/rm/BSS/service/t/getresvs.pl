#!/usr/bin/perl -w

use strict;

use BSS::Client::SOAPClient;

use Data::Dumper;

my( %params );
my $numArgs = $#ARGV + 1;

$params{form_type} = 'admin';

if ($numArgs == 2) {
    if ($ARGV[0] eq '-u') {
        # only get the reservations for that user
        $params{user_dn} = $ARGV[1];
    }
    else {
        # only get the reservation with that particular key
        $params{reservation_id} = $ARGV[1];
    }
}

my($unused, %data) = soap_get_reservations(\%params);
if (defined($data{error_msg}) && $data{error_msg}) {
    print $data{error_msg}, "\n\n";
}
elsif (defined($data{status_msg})) {
    my ($rows, $r, $f, %c);

    $rows = $data{rows};
    for $r (@$rows) {
        for $f (keys %$r) {
            if (defined($r->{$f})) {
                print "$f -> $r->{$f}\n";
            }
        }
        print "\n";
    }
}
