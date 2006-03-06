#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;

use OSCARS::Database;
use OSCARS::BSS::JnxSNMP;
use OSCARS::BSS::RouteHandler;

my $dbconn = OSCARS::Database->new();
$dbconn->connect('BSS');

my $rh = OSCARS::BSS::RouteHandler->new('user' => $dbconn);
my $configs = $rh->get_snmp_configs();
my $dst = 'lbl-rt3';

# Create a query object instance
my $jnxSNMP = OSCARS::BSS::JnxSNMP->new();
ok($jnxSNMP);

$jnxSNMP->initialize_session($configs, $dst);

my $ipaddr = '134.55.210.194';
# Get AS number from IP address.
my $as_number = $jnxSNMP->query_as_number($ipaddr);
my $error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
else { print STDERR "\nAS number: $as_number\n"; }
ok(!$error);

# Get LSP SNMP data.
$jnxSNMP->query_lsp_snmpdata($configs, $dst);
$error = $jnxSNMP->get_error();
if ($error) { print STDERR $error; }
ok(!$error);

$jnxSNMP->close_session();
