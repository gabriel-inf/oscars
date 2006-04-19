#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Intradomain::Pathfinder;

my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getDatabase('Intradomain');
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $rh = OSCARS::Intradomain::Pathfinder->new('db' => $dbconn);
ok($rh);
