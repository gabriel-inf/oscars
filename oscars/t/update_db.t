#!/usr/bin/perl -w

use strict;

use Data::Dumper;

use BSS::SNMP::UpdateDatabase;

if ($#ARGV != 0) { die "usage: update.pl directory\n"; }
my $updater = BSS::SNMP::UpdateDatabase->new(); 
my $params = {'directory' => $ARGV[0]};
$updater->update_router_info($params);
