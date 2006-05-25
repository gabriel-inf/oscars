#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Library::Topology::Pathfinder;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{topology}->{location};
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $rh = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
ok($rh);
