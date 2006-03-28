#!/usr/bin/perl

use Test::Simple tests => 3;

use SOAP::Lite;

use OSCARS::ResourceManager;
use OSCARS::Method;

my $db_name = 'AAAS';
my $component_name = 'AAAS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);

my $status = $rm->use_authentication_plugin('OSCARS::AAAS::AuthN', 'AAAS');
ok($status);

my $client = $rm->add_client();
ok($client);
