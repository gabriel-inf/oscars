#!/usr/bin/perl -w

use SOAP::Lite;

my $soap = SOAP::Lite
  -> uri('http://localhost:2000/AAASServer')
  -> proxy ('http://localhost:2000/soapserver.pl');

print $soap
  -> login('foo')
  -> result;

print "\n\n"
