#!/usr/bin/perl -w

use SOAP::Lite;

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

$params{server_name} = 'BSS';
$params{method} = 'delete_reservation';
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
    exit;
}

print STDERR "Successfully cancelled reservation $params{reservation_id}\n";

