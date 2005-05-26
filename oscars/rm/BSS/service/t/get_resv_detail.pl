#!/usr/bin/perl -w

use BSS::Client::SOAPClient;
use Data::Dumper;

my($value);
my %params = ('reservation_id' => '1' );   # FIX
    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'reservation_start_time', 'reservation_end_time', 'reservation_created_time', 'reservation_bandwidth', 'reservation_burst_limit', 'reservation_status', 'src_hostaddrs_id', 'dst_hostaddrs_id', 'reservation_description', 'reservation_tag' );

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
                if (($key ne 'src_hostaddrs_id') && ($key ne 'dst_hostaddrs_id'))
                {
                    print "$key -> $value\n";
                }
            }
            else { print "$key -> \n"; }
        }
    }
}
