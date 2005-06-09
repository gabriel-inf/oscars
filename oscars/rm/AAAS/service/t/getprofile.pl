#!/usr/bin/perl

use lib '../../..';

use AAAS::Client::SOAPClient;
use Data::Dumper;

my($value);
my %params = ('user_dn' => 'oscars' );

    # names of the fields to be displayed on the screen
my($unused, $results) = soap_get_profile(\%params);
if (defined($results->{error_msg}) && $results->{error_msg})
{
    print $results->{error_msg}, "\n\n";
}
elsif (defined($results->{status_msg}))
{
    print "Status:  ", $results->{status_msg}, "\n";
    print "Returning:\n\n";
    my @data = @{$results->{rows}}[0];
    foreach $key(sort keys %{$data[0]} )
    {
        if ($key ne 'status_msg')
        {
            $value = $data[0]->{$key};
            if ($value) { print "$key -> $value\n"; }
            else { print "$key -> \n"; }
        }
    }
}
