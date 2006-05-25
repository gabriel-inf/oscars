#!/usr/bin/perl

use Test::Simple tests => 4;

use strict;

use OSCARS::PluginManager;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
ok( $pluginMgr );
my $configuration = $pluginMgr->getConfiguration();
ok( $configuration );

my $authN = $pluginMgr->usePlugin('authentication');
ok( $authN );

my $authZ = $pluginMgr->usePlugin('authorization');
ok( $authZ );

