#!/usr/bin/perl -w

require '../soapclient.pl';

my($value);
my %params = ('loginname' => 'davidr' );
my($unused, %results) = soap_get_user_profile(%params);
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
