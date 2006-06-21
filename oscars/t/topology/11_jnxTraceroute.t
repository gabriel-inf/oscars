#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Library::Topology::JnxTraceroute;
use OSCARS::Logger;

my $logger = OSCARS::Logger->new('method' => '11_jnxTraceroute.t');
$logger->set_level($NetLogger::INFO);
$logger->setUserLogin('nologin');
$logger->open('test.log');

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();

my $paramsMgr = OSCARS::PluginManager->new('location' => 'params.xml');
my $params = $paramsMgr->getConfiguration()->{test};
my $database = $configuration->{database}->{topology}->{location};
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

# Create a traceroute object.
my $jnxTraceroute = OSCARS::Library::Topology::JnxTraceroute->new(
                                           'db' => $dbconn,
                                           'logger' => $logger );
ok( $jnxTraceroute );

my $src = $params->{'11_jnxTraceroute'}->{'ingress_loopback'};
my $dst = $params->{'11_jnxTraceroute'}->{'egress_loopback'};
$jnxTraceroute->traceroute( $src, $dst, $logger );
my @rawTracerouteData = $jnxTraceroute->getRawHopData();
ok( @rawTracerouteData );
my @hops = $jnxTraceroute->getHops();
ok( @hops );
$logger->close();
