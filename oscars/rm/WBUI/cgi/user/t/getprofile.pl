#!/usr/bin/perl -w

require '../soapclient.pl';

my %params = ('loginname' => 'davidr' );
my($unused, %results) = soap_get_user_profile(%params);
print $results{'status_msg'}, ' ', $results{'data'}, "\n\n";
