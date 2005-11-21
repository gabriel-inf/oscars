#!/usr/bin/perl

use strict;

use SOAP::Lite;
use Data::Dumper;

my($data, $row, $key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'admin' );
$params{server_name} = 'AAAS';
$params{method} = 'view_users';
my $soap_server = SOAP::Lite
    ->uri('http://198.128.14.164/Dispatcher')
    ->proxy('https://198.128.14.164/SOAP');

my $som = $soap_server->dispatch(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}
my $results = $som->result;
print "Status:  Successfully read user list.\n";
print "Returning:\n\n";
for $row (@$results) {
    for $key (sort keys %$row )
    {
        $value = $row->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
    print "\n";
}
