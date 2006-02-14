#!/usr/bin/perl

use Test::Simple tests => 1;

use SOAP::Lite;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $component_name = 'BSS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
ok($rm);
