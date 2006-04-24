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
$logger->setUserLogin('testaccount');
$logger->open('/home/oscars/logs/test.log');

my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getLocation('system');
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $pf = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
my $configs = $pf->getTracerouteConfig();
ok( $configs );
print STDERR "\n";
print STDERR Dumper($configs);

my $testMgr = TestManager->new('db' => $dbconn,
                                        'database' => $database);
my $testConfigs = $testMgr->getReservationConfigs('jnxTraceroute');

# Create a traceroute object.
my $jnxTraceroute = OSCARS::Library::Topology::JnxTraceroute->new();
ok( $jnxTraceroute );

my $src = $testConfigs->{ingress_loopback};
my $dst = $testConfigs->{egress_loopback};
print STDERR "\nTraceroute: $src to $dst\n";
$jnxTraceroute->traceroute( $configs, $src, $dst, $logger );
print STDERR "Raw results:\n";
my @rawTracerouteData = $jnxTraceroute->getRawHopData();
ok( @rawTracerouteData );

while(defined($rawTracerouteData[0]))  {
    print STDERR '  ' . $rawTracerouteData[0];
    shift(@rawTracerouteData);
}

print STDERR "Hops:\n";
my @hops = $jnxTraceroute->getHops();
ok( @hops );

while(defined($hops[0]))  {
    print STDERR "  $hops[0]\n";
    shift(@hops);
}

print STDERR "\n";
$logger->close();
