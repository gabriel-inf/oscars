#!/usr/bin/perl -w

use AAAS::Client::SOAPClient;

my($result, %data) = soap_logout();
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    print $data{'status_msg'}, "\n\n";
}
