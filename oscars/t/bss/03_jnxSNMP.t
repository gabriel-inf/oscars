#!/usr/bin/perl

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::BSS::JnxSNMP;

##################
# Global variables
##################
my ($_error);
my ($_dst) = 'ga-rt1';
my (@_lspInfo);
my ($_lspName);
my ($_lspVar);
my ($_val);


##################
# Subroutines
##################
sub print_vars  {

  print("Results:\n");
  while (scalar(@_lspInfo))  {
    $_lspVar = shift(@_lspInfo);
    $_val = shift(@_lspInfo);
    print("$_lspVar = $_val\n");
  }
}
#####


##################
# Main
##################
# Create a traceroute object.
my ($_jnxSNMP) = OSCARS::BSS::JnxSNMP->new();

print("Device: $_dst  LSPName: $_lspName  LSPVar: $_lspVar\n");

# Get LSP SNMP data.
$_jnxSNMP->query_lsp_snmpdata($_dst);
if ($_error = $_jnxSNMP->get_error())  {
  die($_error);
}



# Get all LSP info.
undef($_lspName);
undef($_lspVar);

# Print LSP SNMP data.
@_lspInfo = $_jnxSNMP->get_lsp_info($_lspName, $_lspVar);
if ($_error = $_jnxSNMP->get_error())  {
  die($_error);
}
print("\n");
print("Exe: get_lsp_info(undef, undef)\n");
print_vars;



# Get all oscars_ga-nersc_test-be-lsp info.
$_lspName = "oscars_ga-nersc_test-be-lsp";
undef($_lspVar);

# Print LSP SNMP data.
@_lspInfo = $_jnxSNMP->get_lsp_info($_lspName, $_lspVar);
if ($_error = $_jnxSNMP->get_error())  {
  die($_error);
}
print("\n");
print("Exe: get_lsp_info(oscars_ga-nersc_test-be-lsp, undef)\n");
print_vars;



# Get all mplsLspStatesinfo.
undef($_lspName);
$_lspVar = 'mplsLspState';

# Print LSP SNMP data.
@_lspInfo = $_jnxSNMP->get_lsp_info($_lspName, $_lspVar);
if ($_error = $_jnxSNMP->get_error())  {
  die($_error);
}
print("\n");
print("Exe: get_lsp_info(undef, mplsLspState)\n");
print_vars;



# Get all mplsPathRecordRoute for oscars_ga-nersc_test-be-lsp.
$_lspName = 'oscars_ga-nersc_test-be-lsp';
$_lspVar = 'mplsPathRecordRoute';

# Print LSP SNMP data.
@_lspInfo = $_jnxSNMP->get_lsp_info($_lspName, $_lspVar);
if ($_error = $_jnxSNMP->get_error())  {
  die($_error);
}
print("\n");
print("Exe: get_lsp_info(oscars_ga-nersc_test-be-lsp, mplsLspState)\n");
print_vars;

