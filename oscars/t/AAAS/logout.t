#!/usr/bin/perl -w

use AAAS::Client::SOAPClient;

my %params = ('user_dn' => 'dwrobertson@lbl.gov');
$params{method} = 'soap_logout';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}

my $results = $som->result;
print "\nUser $params{user_dn} logged out.\n\n";
