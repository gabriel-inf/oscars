#!/usr/bin/perl

use strict;

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

# Setup an LSP.
print("Setting up LSP...\n");
$jnxLSP->configure_lsp(LSP_SETUP);
print("LSP setup complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is 1=>Up)\n");
my $lspState = $jnxLSP->get_lsp_status();
print("LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n");
print("\n");

# Teardown an LSP.
print("Tearing down LSP...\n");
$jnxLSP->configure_lsp(LSP_TEARDOWN);
print("LSP teardown complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is -1=>NA)\n");
$lspState = $jnxLSP->get_lsp_status();
print("LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n");
