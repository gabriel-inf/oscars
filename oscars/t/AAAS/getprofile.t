#!/usr/bin/perl

use strict;

use AAAS::Client::SOAPClient;
use Data::Dumper;

my($key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'user' );
$params{method} = 'get_profile';
my $som = aaas_dispatcher(\%params);
if ($som->faultstring) {
    print STDERR $som->faultstring, "\n";
    exit;
}
my $results = $som->result;
print "Status:  Retrieved user profile\n";
print "Returning:\n\n";
foreach $key(sort keys %{$results->{row}} )
{
    if (($key ne 'status_msg') &&
        defined($results->{row}->{$key}))
    {
        $value = $results->{row}->{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}
