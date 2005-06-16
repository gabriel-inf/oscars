#!/usr/bin/perl -w

use DateTime;

use BSS::Client::SOAPClient;

my( %params );

$numArgs = $#ARGV + 1;
if ($numArgs == 0) {
    print STDERR "You must provide a reservation id\n";
    exit;
}

    # Delete the reservation with the given id (set its status
    # to cancelled).
$params{'reservation_id'} = $ARGV[0];

my($status, $results);
($status, $results) = soap_delete_reservation(\%params);
if (defined($results->{'error_msg'}) && $results->{'error_msg'})
{
    print $results->{'error_msg'}, "\n\n";
}
elsif (defined($results->{'status_msg'}))
{
    print $results->{'status_msg'}, "\n\n";
}
