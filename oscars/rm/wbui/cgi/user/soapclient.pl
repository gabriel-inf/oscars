#!/usr/bin/perl -w

use SOAP::Lite;

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

sub User_Login
{
    my ($login_name, $password) = @_;
    my $status = $AAAS_server
                 -> login('foo')
                 -> result;
    return($status);
}
