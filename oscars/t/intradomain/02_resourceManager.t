#!/usr/bin/perl

use Test::Simple tests => 1;

use SOAP::Lite;

use OSCARS::ResourceManager;

my $db_name = 'AAA';
my $component_name = 'Intradomain';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);
