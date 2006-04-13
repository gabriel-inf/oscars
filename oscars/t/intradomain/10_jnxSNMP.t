#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;
use Data::Dumper;

use OSCARS::Database;
use OSCARS::Intradomain::JnxSNMP;
use OSCARS::Intradomain::RouteHandler;

my $dbconn = OSCARS::Database->new();
$dbconn->connect('Intradomain');

my $rh = OSCARS::Intradomain::RouteHandler->new('user' => $dbconn);
my $configs = $rh->get_snmp_configs();
ok( $configs );

my $test_configs = $rh->get_test_configs('jnxSNMP');

# name of edge router to perform query on
my $router_name = $test_configs->{router_name};

# Create a query object instance
my $jnxSNMP = OSCARS::Intradomain::JnxSNMP->new();
ok($jnxSNMP);

print STDERR "router_name: $router_name\n";
$jnxSNMP->initialize_session($configs, $router_name);

my $ipaddr = $test_configs->{next_hop};
print STDERR "next hop: $ipaddr\n";
# Get AS number from IP address.
my $as_number = $jnxSNMP->query_as_number($ipaddr);
my $error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
else { print STDERR "\nAS number: $as_number\n"; }
ok(!$error);

$jnxSNMP->close_session();
exit;


# TODO:  tests to be added later

my( $val, $lspName, $lspVar );
print STDERR "Device: $router_name  LSPName: $lspName  LSPVar: $lspVar\n";

# Get LSP SNMP data.
$jnxSNMP->query_lsp_snmpdata($configs, $router_name);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

# Print LSP SNMP data.
my @lspInfo = $jnxSNMP->get_lsp_info($lspName, $lspVar);
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
#
sub print_vars  {

  print STDERR "Results:\n";
  while (scalar(@lspInfo))  {
    $lspVar = shift(@lspInfo);
    $val = shift(@lspInfo);
    print STDERR "$lspVar = $val\n";
  }
} #___________________________________________________________________________

