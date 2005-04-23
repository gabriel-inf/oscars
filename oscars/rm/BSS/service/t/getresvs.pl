#!/usr/bin/perl -w

require 'soapclient.pl';

my($value);
my %params = ('id' => 131 );   # FIX
    # names of the fields to be displayed on the screen
my @fields_to_display = ( 'start_time', 'end_time', 'created_time', 'bandwidth', 'resv_class', 'burst_limit', 'status', 'src_id', 'dst_id', 'dn', 'description' );

my($unused, %results) = soap_get_reservations(\%params, \@fields_to_display);
print "Status:  ", $results{'status_msg'}, "\n";
print "Returning:\n\n";
foreach $key(sort keys %results)
{
    if ($key ne 'status_msg')
    {
        $value = $results{$key};
        if ($value) { print "$key -> $value\n"; }
        else { print "$key -> \n"; }
    }
}
