#!/usr/bin/perl -w

use BSS::Client::SOAPClient;

use Data::Dumper;

my($value);
my %params = ('user_dn' => 'oscars' );   # FIX
    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'user_dn', 'reservation_start_time', 'reservation_end_time', 'reservation_status', 'src_hostaddrs_id', 'dst_hostaddrs_id' );

my($unused, %data) = soap_get_reservations(\%params, \@fields_to_read);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    my ($rows, $r, $f);

    foreach $_ (@fields_to_read)
    {
        print $_, ' ';
    }
    print "\n";
    $rows = $data{'rows'};
    foreach $r (@$rows)
    {
        print $r->{'user_dn'}, ' ';
        print $r->{'reservation_start_time'}, ' ';
        print $r->{'reservation_end_time'}, ' ';
        print $r->{'reservation_status'}, ' ';;
        print $r->{'src_hostaddrs_id'}, ' ';
        print $r->{'dst_hostaddrs_id'};
        print "\n";
    }
}
