#!/usr/bin/perl -w

use strict;

use AAAS::Client::SOAPClient;

# This will go outside of repository.

my %params = ('user_dn' => 'oscars', 'user_password' => 'ritazza6');
#$params{'admin_required'} = 1;
my($result, %data) = soap_verify_login(\%params);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    print $data{'status_msg'}, "\n\n";
}
