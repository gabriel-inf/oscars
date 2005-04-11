#!/usr/bin/perl -w

use SOAP::Lite;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

sub User_Login
{
    my ($loginname, $password) = @_;
    my $response = $AAAS_server -> login($loginname, $password);
    my $error_status = $response->result();
    if ($response->fault) {
        print $response->faultcode, " ", $response->faultstring, "\n";
    }
    my @error_message = $response->paramsout();
    return ($error_status, @error_message);
}
