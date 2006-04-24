#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;
use Data::Dumper;

use TestManager;
use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Library::Topology::JnxSNMP;
use OSCARS::Library::Topology::Pathfinder;

my $pluginMgr = OSCARS::PluginManager->new();
my $database = $pluginMgr->getLocation('system');
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $pf = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
my $configs = $pf->getSNMPConfiguration();
ok( $configs );

my $testMgr = TestManager->new('db' => $dbconn,
                                'database' => $database);
my $testConfigs = $testMgr->getReservationConfigs('jnxSNMP');

# name of edge router to perform query on
my $routerName = $testConfigs->{router_name};

# Create a query object instance
my $jnxSnmp = OSCARS::Library::Topology::JnxSNMP->new();
ok($jnxSnmp);

print STDERR "routerName: $routerName\n";
$jnxSnmp->initializeSession($configs, $routerName);

my $ipaddr = $testConfigs->{next_hop};
print STDERR "next hop: $ipaddr\n";
# Get AS number from IP address.
my $asNumber = $jnxSnmp->queryAsNumber($ipaddr);
my $error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
else { print STDERR "\nAS number: $asNumber\n"; }
ok(!$error);

$jnxSnmp->closeSession();
exit;


# TODO:  tests to be added later

my( $val, $lspName, $lspVar );
print STDERR "Device: $routerName  LSPName: $lspName  LSPVar: $lspVar\n";

# Get LSP SNMP data.
$jnxSnmp->queryLspSnmp($configs, $routerName);
$error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
ok(!$error);

# Print LSP SNMP data.
my @lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\n";
print STDERR "Exe: queryLspInfo(undef, undef)\n";
printVars();


# Get all oscars_ga-nersc_test-be-lsp info.
$lspName = "oscars_ga-nersc_test-be-lsp";
undef($lspVar);

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: queryLspInfo(oscars_ga-nersc_test-be-lsp, undef)\n";
printVars();


# Get all mplsLspStatesinfo.
undef($lspName);
$lspVar = 'mplsLspState';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: queryLspInfo(undef, mplsLspState)\n";
printVars();

# Get all mplsPathRecordRoute for oscars_ga-nersc_test-be-lsp.
$lspName = 'oscars_ga-nersc_test-be-lsp';
$lspVar = 'mplsPathRecordRoute';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: queryLspInfo(oscars_ga-nersc_test-be-lsp, mplsLspState)\n";
printVars();


###############################################################################
#
sub printVars  {

  print STDERR "Results:\n";
  while (scalar(@lspInfo))  {
    $lspVar = shift(@lspInfo);
    $val = shift(@lspInfo);
    print STDERR "$lspVar = $val\n";
  }
} #___________________________________________________________________________

