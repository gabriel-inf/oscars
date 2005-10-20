#!/usr/bin/perl -w

use strict;

use AAAS::Client::SOAPClient;
use Term::ReadKey;
use Data::Dumper;

ReadMode('noecho');
print STDERR "Please enter your OSCARS password: ";
my $password = ReadLine(0);
ReadMode('restore');
print STDERR "\n";
chomp($password);

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => $password);
$params{method} = 'verify_login';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n\n";
    exit;
}
my $results = $som->result;
print "\nUser $params{user_dn} successfully logged in.\n\n";
