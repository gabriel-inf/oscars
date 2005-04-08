#!/usr/bin/perl -w

use SOAP::Lite;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

sub User_Login
{
    my ($login_name, $password) = @_;
    my $response = $AAAS_server -> login($login_name, $password);
    my $error_status = $response->result();
    my @error_message = $response->paramsout();
    return ($error_status, @error_message);
}
