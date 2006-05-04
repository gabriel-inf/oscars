#!/usr/bin/perl

use strict;
use Test::Simple tests => 4;
use Data::Dumper;

use TestManager;
use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Library::Topology::JnxTraceroute;
use OSCARS::Library::Topology::Pathfinder;
use OSCARS::Logger;

my $logger = OSCARS::Logger->new('method' => '11_jnxTraceroute.t');
$logger->set_level($NetLogger::INFO);
$logger->setUserLogin('nologin');
$logger->open('test.log');

my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getLocation('system');
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $pf = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
my $configs = $pf->getTracerouteConfig();
ok( $configs );
my $info = substr(Dumper($configs), 0, -1);
$logger->info('Configs', { 'fields' => $info });

my $testMgr = TestManager->new('db' => $dbconn,
                                        'database' => $database);
my $testConfigs = $testMgr->getReservationConfigs('jnxTraceroute');

# Create a traceroute object.
my $jnxTraceroute = OSCARS::Library::Topology::JnxTraceroute->new();
ok( $jnxTraceroute );

my $src = $testConfigs->{ingress_loopback};
my $dst = $testConfigs->{egress_loopback};
$jnxTraceroute->traceroute( $configs, $src, $dst, $logger );
my @rawTracerouteData = $jnxTraceroute->getRawHopData();
ok( @rawTracerouteData );
my @hops = $jnxTraceroute->getHops();
ok( @hops );
$logger->close();
