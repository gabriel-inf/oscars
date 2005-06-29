#!/usr/bin/perl -w

use strict;

use BSS::Client::SOAPClient;

my %params = ('user_dn' => 'dwrobertson@lbl.gov', 'user_password' => 'Shyysh');
my($result, $data) = soap_login_user(\%params);
if (defined($data->{'error_msg'}) && $data->{'error_msg'})
{
    print $data->{'error_msg'}, "\n\n";
}
elsif (defined($data->{'status_msg'}))
{
    print $data->{'status_msg'}, "\n\n";
}
