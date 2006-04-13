#!/usr/bin/perl

use Test::Simple tests => 3;

use SOAP::Lite;

use OSCARS::ResourceManager;
use OSCARS::Method;

my $db_name = 'AAA';
my $component_name = 'AAA';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);

my $status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');
ok($status);

my $client = $rm->add_client();
ok($client);
