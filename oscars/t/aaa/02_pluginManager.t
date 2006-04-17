#!/usr/bin/perl

use Test::Simple tests => 3;

use strict;

use OSCARS::PluginManager;

my $mgr = OSCARS::PluginManager->new();
ok($mgr);

my $authN = $mgr->use_plugin('authentication');
ok($authN);

my $authZ = $mgr->use_plugin('authorization');
ok($authZ);

