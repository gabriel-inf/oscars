#!/usr/bin/perl -w

use lib '../../..';

use AAAS::Client::SOAPClient;

my($value);
my %params = ('user_dn' => 'dwrobertson@lbl.gov' );
    # names of the fields to be displayed on the screen
my @fields_to_display = ( 'user_last_name', 'user_first_name', 'user_dn', 'user_email_primary', 'user_level', 'user_email_secondary', 'user_phone_primary', 'user_phone_secondary', 'user_description', 'user_register_time', 'user_activation_key', 'institution_id' );

my($unused, %results) = soap_get_profile(\%params, \@fields_to_display);
if (defined($results{'error_msg'}) && $results{'error_msg'})
{
    print $results{'error_msg'}, "\n\n";
}
elsif (defined($results{'status_msg'}))
{
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
}
