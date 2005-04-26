#!/usr/bin/perl -w

use lib '../../..';

use BSS::Client::SOAPClient;

use Data::Dumper;

my($value);
my %params = ('dn' => 'oscars' );   # FIX
    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'start_time', 'end_time', 'bandwidth', 'status', 'src_id', 'dst_id', 'dn' );

my($unused, %data) = soap_get_reservations(\%params, \@fields_to_read);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    my $rows = $data{'rows'};
    my ($r, $f);
    foreach $_ (@fields_to_read)
    {
        print $_, ' ';
    }
    print "\n\n";
    foreach $r (@$rows)
    {
       foreach $f (@$r)
       {
           print $f, ' ';
       }
       print "\n";
    }
    
}
