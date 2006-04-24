#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Library::Topology::Pathfinder;

my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getLocation('system');
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $rh = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
ok($rh);
