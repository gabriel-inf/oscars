#!/usr/bin/perl -w

use SOAP::Lite;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

# TODO:  one SOAP call that dispatches according to subroutine name

sub soap_process_user_login
{
    my (%params) = @_;
    my $response = $AAAS_server -> process_user_login(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}

sub soap_get_user_profile
{
    my (%params) = @_;
    my $response = $AAAS_server -> get_user_profile(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_set_user_profile
{
    my (%params) = @_;
    my $response = $AAAS_server -> set_user_profile(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


my $BSS_server = SOAP::Lite
  -> uri('http://localhost:3000/BSSServer')
  -> proxy ('http://localhost:3000/soapserver.pl');

sub soap_view_reservations
{
    my (%params) = @_;
    my $response = $BSS_server -> view_reservations(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_create_reservation
{
    my (%params) = @_;
    my $response = $BSS_server -> process_create_reservation(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


