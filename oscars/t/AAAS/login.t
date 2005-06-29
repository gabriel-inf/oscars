#!/usr/bin/perl -w

use strict;

use AAAS::Client::SOAPClient;

# This will go outside of repository.

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => 'Shyysh');
my($result, $data) = soap_verify_login(\%params);
print STDERR $data->{'user_level'}, "\n";
if (defined($data->{'error_msg'}) && $data->{'error_msg'})
{
    print $data->{'error_msg'}, "\n\n";
}
elsif (defined($data->{'status_msg'}))
{
    print $data->{'status_msg'}, "\n\n";
}
