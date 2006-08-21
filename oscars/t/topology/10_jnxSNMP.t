#!/usr/bin/perl

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

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

