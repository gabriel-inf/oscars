#!/usr/bin/perl

use strict;

use AAAS::Client::SOAPClient;
use Data::Dumper;

my($data, $row, $key, $value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov',
              'user_level' => 'admin' );

    # names of the fields to be displayed on the screen
my($unused, $results) = soap_get_userlist(\%params);
if (defined($results->{error_msg}) && $results->{error_msg})
{
    print $results->{error_msg}, "\n\n";
}
elsif (defined($results->{status_msg}))
{
    print "Status:  ", $results->{status_msg}, "\n";
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
}
