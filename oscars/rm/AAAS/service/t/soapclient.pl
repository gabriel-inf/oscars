#!/usr/bin/perl -w

use SOAP::Lite;

use strict;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

# TODO:  one SOAP call that dispatches according to server, subroutine args

sub soap_verify_login
{
    my ($params) = @_;
    my $response = $AAAS_server -> Verify_login($params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    return ($response->result(), $response->paramsout());
}

sub soap_get_profile
{
    my ($params, $fields_to_display) = @_;
    my $response = $AAAS_server -> Get_profile($params, $fields_to_display);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}


sub soap_set_profile
{
    my (%params) = @_;
    my $response = $AAAS_server -> Set_profile(%params);
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
        #  params are either user profile, or error message
    return ($response->result(), $response->paramsout());
}
