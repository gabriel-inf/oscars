#!/usr/bin/perl -w

require 'soapclient.pl';

my($value);
my %params = ('id' => 131 );   # FIX
    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'start_time', 'end_time', 'created_time', 'bandwidth', 'resv_class', 'burst_limit', 'status', 'src_id', 'dst_id', 'dn', 'description' );

my($unused, %data) = soap_get_reservations(\%params, \@fields_to_read);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    print "Status:  ", $data{'status_msg'}, "\n";
    print "Returning:\n\n";
    foreach $key(sort keys %data)
    {
        if ($key ne 'status_msg')
        {
            $value = $data{$key};
            if ($value) { print "$key -> $value\n"; }
            else { print "$key -> \n"; }
        }
    }
}
