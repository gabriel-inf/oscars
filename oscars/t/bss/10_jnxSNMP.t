#!/usr/bin/perl

use strict;
use Test::Simple tests => 5;

use OSCARS::Database;
use OSCARS::BSS::JnxSNMP;

my $dbconn = OSCARS::Database->new();
$dbconn->connect('BSS');
my $configs = get_pss_configs($dbconn);

my $dst = 'dc-cr1';
my ( $error, @lspInfo, $lspName, $lspVar, $val );


# Create a query object instance
my $jnxSNMP = OSCARS::BSS::JnxSNMP->new();

print STDERR "Device: $dst  LSPName: $lspName  LSPVar: $lspVar\n";

# Get LSP SNMP data.
$jnxSNMP->query_lsp_snmpdata($configs, $dst);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

# Print LSP SNMP data.
@lspInfo = $jnxSNMP->get_lsp_info($lspName, $lspVar);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\n";
print STDERR "Exe: get_lsp_info(undef, undef)\n";
print_vars();


# Get all oscars_ga-nersc_test-be-lsp info.
$lspName = "oscars_ga-nersc_test-be-lsp";
undef($lspVar);

# Print LSP SNMP data.
@lspInfo = $jnxSNMP->get_lsp_info($lspName, $lspVar);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: get_lsp_info(oscars_ga-nersc_test-be-lsp, undef)\n";
print_vars();


# Get all mplsLspStatesinfo.
undef($lspName);
$lspVar = 'mplsLspState';

# Print LSP SNMP data.
@lspInfo = $jnxSNMP->get_lsp_info($lspName, $lspVar);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: get_lsp_info(undef, mplsLspState)\n";
print_vars();

# Get all mplsPathRecordRoute for oscars_ga-nersc_test-be-lsp.
$lspName = 'oscars_ga-nersc_test-be-lsp';
$lspVar = 'mplsPathRecordRoute';

# Print LSP SNMP data.
@lspInfo = $jnxSNMP->get_lsp_info($lspName, $lspVar);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

print STDERR "\nExe: get_lsp_info(oscars_ga-nersc_test-be-lsp, mplsLspState)\n";
print_vars();


###############################################################################
# Adapted from OSCARS::BSS::RouteHandler->get_pss_configs
#
sub get_pss_configs {
    my( $dbconn ) = @_;

        # use defaults for now
    my $statement = 'SELECT ' .
             'pss_conf_access, pss_conf_login, pss_conf_passwd, ' .
             'pss_conf_firewall_marker, ' .
             'pss_conf_setup_file, pss_conf_teardown_file, ' .
             'pss_conf_ext_if_filter, pss_conf_CoS, ' .
             'pss_conf_burst_limit, ' .
             'pss_conf_setup_priority, pss_conf_resv_priority, ' .
             'pss_conf_allow_lsp '  .
             'FROM pss_confs where pss_conf_id = 1';
    my $configs = $dbconn->get_row($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub print_vars  {

  print STDERR "Results:\n";
  while (scalar(@lspInfo))  {
    $lspVar = shift(@lspInfo);
    $val = shift(@lspInfo);
    print STDERR "$lspVar = $val\n";
  }
} #___________________________________________________________________________


