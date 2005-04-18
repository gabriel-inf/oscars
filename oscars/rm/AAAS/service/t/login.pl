#!/usr/bin/perl -w

require 'soapclient.pl';

my $passwd = crypt('shyysh', 'oscars');
my %params = ('dn' => 'davidr', 'password' => $passwd);
my($result, %data) = soap_verify_login(\%params);
print $data{'status_msg'}, "\n\n";
