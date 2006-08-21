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
use Test qw(plan ok skip);

plan tests => 5;

use TestManager;
use OSCARS::PSS::JnxLSP;
use OSCARS::PluginManager;
use OSCARS::Database;
use OSCARS::Logger;

use constant LSP_SETUP => 1;
use constant LSP_TEARDOWN => 0;

#####
#
# When creating a JnxLSP object, the following parameters can be set:
# - name => string that uniquely identifies a reservation (required)
# - lsp_from => router that will initiate (start-point) LSP (required)
# - lsp_to => router that will terminate LSP (required for setup)
# - bandwidth => LSP bandwidth (e.g. 20, 20k, 20m) (required for setup)
# - lsp_class-of-service => LSP class-of-service (required for setup)
# - lsp_setup-priority => LSP setup priority (optional)
# - lsp_reservation-priority => LSP reservation priority (optional)
# - lsp_description => LSP description (optional)
# - policer_burst-size-limit => LSP burst size limit, typically 10% of
#     bandwidth (required for setup)
# - source-address => IP/network of source (e.g. 10.0.0.1, 10.10.10.0/24)
#     (required for setup)
# - destination-address => destination IP/network of sink
#     (e.g. 10.0.0.1, 10.10.10.0/24) (required for setup)
# - dscp => DSCP value of traffic (optional)
# - protocol => protocol number of traffic (optional)
# - source-port => port number of source traffic (optional)
# - destination-port => port number of destination traffic (optional)
#
#####

my $logger = OSCARS::Logger->new('method' => '01_jnxLSP.t');
$logger->set_level($NetLogger::INFO);
$logger->setUserLogin('nologin');
$logger->open('test.log');

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{topology}->{location};
my $dbconn = OSCARS::Database->new();
$dbconn->connect($database);

my $testMgr = TestManager->new();
my $testConfigs = $testMgr->getReservationConfigs('jnxLSP');

# Initialize LSP information.
my %lspInfo = (
  'name' => 'oscars_jnxLSP_test',
  'lsp_from' => $testConfigs->{ingress_loopback},
  'lsp_to' => $testConfigs->{egress_loopback},
  'bandwidth' => '1000000',
  'lsp_class-of-service' => '4',
  'policer_burst-size-limit' => '1000000',
  'source-address' => $testConfigs->{reservation_source},
  'destination-address' => $testConfigs->{reservation_destination},
  'protocol' => 'udp',
  'logger' => $logger,
  'db' => $dbconn,
);

# Create an LSP object.
my $jnxLSP = new OSCARS::PSS::JnxLSP(\%lspInfo);
ok($jnxLSP);

# Setup an LSP.
print("Setting up LSP...\n");
$jnxLSP->configure_lsp(LSP_SETUP);
ok($jnxLSP->get_error());
print("LSP setup complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is 1=>Up)\n");
my $lspState = $jnxLSP->get_lsp_status();
ok($jnxLSP->get_error());
print("LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n");
print("\n");

# Teardown an LSP.
print("Tearing down LSP...\n");
$jnxLSP->configure_lsp(LSP_TEARDOWN);
ok($jnxLSP->get_error());
print("LSP teardown complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is -1=>NA)\n");
$lspState = $jnxLSP->get_lsp_status();
ok($jnxLSP->get_error());
print("LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n");
