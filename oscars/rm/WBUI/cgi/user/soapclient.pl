#!/usr/bin/perl -w

use SOAP::Lite;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/AAAS_Server.pl');

# TODO:  one SOAP call that dispatches according to server, subroutine args

sub soap_verify_login
{
    my ($params) = @_;
    my $response = $AAAS_server -> verify_login($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}

sub soap_get_profile
{
    my ($params, $fields_to_display ) = @_;
    my $response = $AAAS_server -> get_profile($params, $fields_to_display);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_set_profile
{
    my ($params) = @_;
    my $response = $AAAS_server -> set_profile($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


my $BSS_server = SOAP::Lite
  -> uri('http://localhost:3000/BSS_Server')
  -> proxy ('http://localhost:3000/BSS_server.pl');

sub soap_get_reservations
{
    my ($params) = @_;
    my $response = $BSS_server -> get_reservations($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_create_reservation
{
    my ($params) = @_;
    my $response = $BSS_server -> create_reservation($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_remove_reservation
{
    my ($params) = @_;
    my $response = $BSS_server -> remove_reservation($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}

