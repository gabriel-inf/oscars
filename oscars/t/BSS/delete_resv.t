#!/usr/bin/perl -w

use DateTime;

use BSS::Client::SOAPClient;

my( %params );

$numArgs = $#ARGV + 1;
if ($numArgs < 2) {
    print STDERR "You must provide a reservation id and a user dn\n";
    exit;
}

    # Delete the reservation with the given id (set its status
    # to cancelled).
$params{'reservation_id'} = $ARGV[0];
$params{'user_dn'} = $ARGV[1];

$params{method} = 'soap_delete_reservation';
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

print STDERR "Successfully cancelled reservation $params{reservation_id}\n";

