#!/usr/bin/perl -w

use SOAP::Lite;


my $BSS_server = SOAP::Lite
  -> uri('http://localhost:5000/BSS_Server')
  -> proxy ('http://localhost:5000/soapserver.pl');

sub soap_get_reservations
{
    my (%params) = @_;
    my $response = $BSS_server -> Get_reservations(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_create_reservation
{
    my ($inref) = @_;
    my $response = $BSS_server -> Create_reservation($inref);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_remove_reservation
{
    my (%params) = @_;
    my $response = $BSS_server -> Remove_reservation(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}

