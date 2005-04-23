#!/usr/bin/perl -w

#use SOAP::Lite +trace;
use SOAP::Lite;

use lib '../../..';

use BSS::Scheduler::ReservationHandler;


my $BSS_server = SOAP::Lite
  -> uri('http://localhost:3000/BSS/Scheduler/ReservationHandler')
  -> proxy ('http://localhost:3000/soapserver.pl');

sub soap_get_reservations
{
    my ($inref, $fields_to_display) = @_;
    my $response = $BSS_server -> get_reservations($inref, $fields_to_display);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_create_reservation
{
    my ($inref) = @_;
    my $response = $BSS_server -> create_reservation($inref);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_remove_reservation
{
    my (%params) = @_;
    my $response = $BSS_server -> remove_reservation(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}

