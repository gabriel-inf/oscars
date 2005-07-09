#!/usr/bin/perl

use strict;

use AAAS::Client::SOAPClient;
use Data::Dumper;

my($data, $row, $key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'admin' );
$params{method} = 'soap_get_userlist';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring;
    exit;
}

my $results = $som->result;
if ($results->{error_msg}) {
    print STDERR $results->{error_msg}, "\n\n";
    exit;
}

print "Status:  Successfully read user list.\n";
print "Returning:\n\n";
$data = $results->{rows};
for $row (@$data) {
    for $key (sort keys %$row )
    {
        $value = $row->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
    print "\n";
}
