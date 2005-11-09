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
$params{method} = 'verify_login';
my $aaas_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/AAAS');
    #->uri('http://127.0.0.1/Dispatcher')
    #->proxy('https://127.0.0.1/AAAS');

my $som = $aaas_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n\n";
    exit;
}
my $results = $som->result;
print "\nUser $params{user_dn} successfully logged in.\n\n";
