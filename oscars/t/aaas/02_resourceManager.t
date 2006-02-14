#!/usr/bin/perl

use Test::Simple tests => 3;

use SOAP::Lite;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);

my $portnum = $rm->get_daemon_info($component_name);
ok($portnum);

my $client = $rm->add_client($component_name);
ok($client);
