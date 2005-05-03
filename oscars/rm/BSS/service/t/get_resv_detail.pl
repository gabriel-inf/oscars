#!/usr/bin/perl -w

use BSS::Client::SOAPClient;
use Data::Dumper;

my($value);
my %params = ('id' => '10' );   # FIX
    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'start_time', 'end_time', 'created_time', 'bandwidth', 'burst_limit', 'status', 'src_id', 'dst_id', 'description' );

my($unused, %results) = BSS::Client::SOAPClient::soap_get_resv_detail(\%params, \@fields_to_read);
if (defined($results{'error_msg'}) && $results{'error_msg'})
{
    print $results{'error_msg'}, "\n\n";
}
elsif (defined($results{'status_msg'}))
{
    my ($rows, $r, $f);

    print "Status:  ", $results{'status_msg'}, "\n";
    print "Returning:\n\n";
    foreach $key(sort keys %results)
    {
        if ($key ne 'status_msg')
        {
            $value = $results{$key};
            if ($value) {
                if (($key ne 'src_id') && ($key ne 'dst_id'))
                {
                    print "$key -> $value\n";
                }
            }
            else { print "$key -> \n"; }
        }
    }
}
