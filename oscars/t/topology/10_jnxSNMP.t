#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;
use Data::Dumper;

use NetLogger;

use TestManager;
use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Logger;

use OSCARS::Library::Topology::JnxSNMP;
use OSCARS::Library::Topology::Pathfinder;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{topology}->{location};
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $logger = OSCARS::Logger->new('method' => '10_jnxSNMP');
$logger->set_level($NetLogger::INFO);
$logger->setUserLogin('nologin');
$logger->open('test.log');

my $pf = OSCARS::Library::Topology::Pathfinder->new('db' => $dbconn);
my $configs = $pf->getSNMPConfiguration();
ok( $configs );

my $testMgr = TestManager->new();
my $testConfigs = $testMgr->getReservationConfigs('jnxSNMP');

# name of edge router to perform query on
my $routerName = $testConfigs->{router_name};

# Create a query object instance
my $jnxSnmp = OSCARS::Library::Topology::JnxSNMP->new();
ok($jnxSnmp);

$logger->info("Router", { 'name' => $routerName });
$jnxSnmp->initializeSession($configs, $routerName);

my $ipaddr = $testConfigs->{next_hop};
$logger->info( "Next", { 'hop' => $ipaddr } );
# Get AS number from IP address.
my $asNumber = $jnxSnmp->queryAsNumber($ipaddr);
my $error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
else { $logger->info( "AS", { 'number' => $asNumber } ); }
ok(!$error);

$jnxSnmp->closeSession();
$logger->close();
exit;


# TODO:  tests to be added later

my( $val, $lspName, $lspVar );
$logger->info( "Device", { 'name' => $routerName } );
$logger->info( "LSP", { 'name' => $lspName } );
$logger->info( "LSP", { 'var' => $lspVar } );

# Get LSP SNMP data.
$jnxSnmp->queryLspSnmp($configs, $routerName);
$error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
ok(!$error);

# Print LSP SNMP data.
my @lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
ok(!$error);

$logger->info( "Exe", { 'method' => "queryLspInfo(undef, undef)" } );
logVars();


# Get all oscars_ga-nersc_test-be-lsp info.
$lspName = "oscars_ga-nersc_test-be-lsp";
undef($lspVar);

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
ok(!$error);

$logger->info( "Exe", { 'method' => "queryLspInfo($lspName, undef)" } );
logVars();


# Get all mplsLspStatesinfo.
undef($lspName);
$lspVar = 'mplsLspState';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
ok(!$error);

$logger->info( "Exe", { 'method' => "queryLspInfo(undef, mplsLspState)" } );
logVars();

# Get all mplsPathRecordRoute for oscars_ga-nersc_test-be-lsp.
$lspName = 'oscars_ga-nersc_test-be-lsp';
$lspVar = 'mplsPathRecordRoute';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$error = $jnxSnmp->getError();
if ($error) { $logger->warn( "Error", { '' => $error } ); }
ok(!$error);

$logger->info( "Exe", { 'method' => "queryLspInfo($lspName, mplsLspState)" } );
logVars();


###############################################################################
#
sub logVars  {

  my $vars = {};
  while (scalar(@lspInfo))  {
    $lspVar = shift(@lspInfo);
    $val = shift(@lspInfo);
    $vars->{$lspVar} = $val;
  }
  $logger->info( "Vars", { '' => $vars } );
} #___________________________________________________________________________

