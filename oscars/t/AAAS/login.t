#!/usr/bin/perl -w

use strict;

use AAAS::Client::SOAPClient;
use Data::Dumper;

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => 'Shyysh');
#my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => '');
$params{method} = 'verify_login';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n\n";
    exit;
}
my $results = $som->result;
print "\nUser $params{user_dn} successfully logged in.\n\n";
