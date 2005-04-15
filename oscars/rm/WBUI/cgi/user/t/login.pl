#!/usr/bin/perl -w

require '../soapclient.pl';

my $passwd = crypt('shyysh', 'oscars');
my %params = ('loginname' => 'davidr', 'password' => $passwd);
my($result, %data) = soap_process_user_login(%params);
print $data{'status_msg'}, "\n\n";
