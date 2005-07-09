#!/usr/bin/perl -w

use AAAS::Client::SOAPClient;

my %params = ('user_dn' => 'dwrobertson@lbl.gov');
$params{method} = 'soap_logout';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}

my $results = $som->result;
if ($results->{'error_msg'}) {
    print STDERR $results->{error_msg}, "\n\n";
}
else {
    print "\nUser $params{user_dn} logged out.\n\n";
}
