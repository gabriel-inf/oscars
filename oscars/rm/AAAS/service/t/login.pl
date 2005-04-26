#!/usr/bin/perl -w

use lib '../../..';

use AAAS::Client::SOAPClient;


my $passwd = crypt('shyysh', 'oscars');
my %params = ('dn' => 'davidr', 'password' => $passwd);
my($result, %data) = soap_verify_login(\%params);
if (defined($data{'error_msg'}) && $data{'error_msg'})
{
    print $data{'error_msg'}, "\n\n";
}
elsif (defined($data{'status_msg'}))
{
    print $data{'status_msg'}, "\n\n";
}
