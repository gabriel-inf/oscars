#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::PluginManager;
use OSCARS::MethodFactory;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);

my $factory = OSCARS::MethodFactory->new('pluginMgr' => $pluginMgr);
ok($factory);
