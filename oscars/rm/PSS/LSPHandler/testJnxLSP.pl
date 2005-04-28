#!/usr/local/bin/perl

use strict;

use lib '../..';
use PSS::module::JnxLSP;

#####
#
# Constant definitions.
#
#####
use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;


#####
#
# Global variables.
#
#####
my ($_error);
my ($_lspState);


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

# Initialize LSP information.
my (%_lspInfo) = (
  'name' => 'oscars_resvID_oscars',
  'lsp_from' => 'dev-rt20-e.es.net',
  'lsp_to' => '10.0.0.1',
  'bandwidth' => '29m',
  'lsp_class-of-service' => '4',
  'policer_burst-size-limit' => '1m',
  'source-address' => '192.168.2.2',
  'destination-address' => '192.168.0.2',
  'dscp' => 'ef',
  'protocol' => 'udp',
  'source-port' => '5000',
);

# Create an LSP object.
my ($_jnxLsp) = new JnxLSP(%_lspInfo);


# Setup an LSP.
print("Setting up LSP...\n");
$_jnxLsp->configure_lsp(_LSP_SETUP);
if ($_error = $_jnxLsp->get_error())  {
  die($_error);
}
print("LSP setup complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is 1=>Up)\n");
$_lspState = $_jnxLsp->get_lsp_status();
if ($_error = $_jnxLsp->get_error())  {
  die($_error);
}
print("LSP State: $_lspState (-1=>NA, 0=>Down, 1=>Up)\n");
print("\n");


# Teardown an LSP.
print("Tearing down LSP...\n");
$_jnxLsp->configure_lsp(_LSP_TEARDOWN);
if ($_error = $_jnxLsp->get_error())  {
  die($_error);
}
print("LSP teardown complete\n");
print("\n");

# Check that state of the LSP.
print("Checking LSP state...  (expected result is -1=>NA)\n");
$_lspState = $_jnxLsp->get_lsp_status();
if ($_error = $_jnxLsp->get_error())  {
  die($_error);
}
print("LSP State: $_lspState (-1=>NA, 0=>Down, 1=>Up)\n");
