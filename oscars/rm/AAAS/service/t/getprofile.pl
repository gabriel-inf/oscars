#!/usr/bin/perl -w

require 'soapclient.pl';

my($value);
my %params = ('dn' => 'davidr' );
    # names of the fields to be displayed on the screen
my @fields_to_display = ( 'last_name', 'first_name', 'dn', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'level', 'register_time', 'activation_key', 'pending_level', 'authorization_id', 'institution_id' );

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
