#!/usr/bin/perl -w

use strict;

use AAAS::Client::SOAPClient;
use Data::Dumper;

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => 'Shyysh');
#my %params = ('user_dn' => 'foo@lbl.gov', 'user_password' => 'Shyysh');
$params{method} = 'soap_verify_login';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
}
else {
    print "\nUser $params{user_dn} successfully logged in.\n\n";
}
