#!/usr/bin/perl

use strict;
use Test::Simple tests => 4;
use Socket;
use Data::Dumper;

use NetLogger;

use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Logger;

use OSCARS::Library::Topology::JnxSNMP;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();

my $paramsMgr = OSCARS::PluginManager->new('location' => 'params.xml');
my $params = $paramsMgr->getConfiguration()->{test};
my $database = $configuration->{database}->{topology}->{location};
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $logger = OSCARS::Logger->new('method' => '10_jnxSNMP');
$logger->set_level($NetLogger::INFO);
$logger->setUserLogin('nologin');
$logger->setMethod('10_jnxSNMP');
$logger->open('test.log');

# name of edge router to perform query on
my $routerName = $params->{'10_jnxSNMP'}->{'router_name'};

# Create a query object instance
my $jnxSnmp = OSCARS::Library::Topology::JnxSNMP->new('db' => $dbconn );
ok($jnxSnmp);

my $configs = $jnxSnmp->getConfigs();
ok( $configs );

$jnxSnmp->initializeSession( $routerName );
my $errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

my $hostname = $params->{'10_jnxSNMP'}->{'next_hop'};
my $ipaddr = inet_ntoa(inet_aton($hostname));
$logger->info("NextHop", { 'IP' => $ipaddr } ); 

# Get AS number from IP address.
my $asNumber = $jnxSnmp->queryAsNumber($ipaddr);
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
else { $logger->info( "AS", { 'number' => $asNumber } ); }
ok(!$errorMsg);

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
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

# Print LSP SNMP data.
my @lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

$logger->info( "Exe", { 'method' => "queryLspInfo(undef, undef)" } );
logVars();


# Get all oscars_ga-nersc_test-be-lsp info.
$lspName = "oscars_ga-nersc_test-be-lsp";
undef($lspVar);

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

$logger->info( "Exe", { 'method' => "queryLspInfo($lspName, undef)" } );
logVars();


# Get all mplsLspStatesinfo.
undef($lspName);
$lspVar = 'mplsLspState';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

$logger->info( "Exe", { 'method' => "queryLspInfo(undef, mplsLspState)" } );
logVars();

# Get all mplsPathRecordRoute for oscars_ga-nersc_test-be-lsp.
$lspName = 'oscars_ga-nersc_test-be-lsp';
$lspVar = 'mplsPathRecordRoute';

# Print LSP SNMP data.
@lspInfo = $jnxSnmp->queryLspInfo($lspName, $lspVar);
$errorMsg = $jnxSnmp->getError();
if ($errorMsg) { $logger->warn( "Error", { '' => $errorMsg } ); }
ok(!$errorMsg);

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

