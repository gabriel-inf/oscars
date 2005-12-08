#!/usr/bin/perl -w

use strict;

use SOAP::Lite;
use Term::ReadKey;
use Data::Dumper;

ReadMode('noecho');
print STDERR "Please enter your OSCARS password: ";
my $password = ReadLine(0);
ReadMode('restore');
print STDERR "\n";
chomp($password);

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => $password);
$params{server_name} = 'AAAS';
$params{method} = 'login';
$params{user_level} = 2;
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n\n";
    exit;
}
my $results = $som->result;
print "\nUser $params{user_dn} successfully logged in.\n\n";
