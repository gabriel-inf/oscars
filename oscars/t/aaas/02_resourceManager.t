#!/usr/bin/perl

use Test::Simple tests => 2;

use SOAP::Lite;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'AAAS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);

my $client = $rm->add_client();
ok($client);
