#!/usr/bin/perl

use strict;
use Test::Simple tests => 4;
use Data::Dumper;

use OSCARS::Database;
use OSCARS::Intradomain::JnxTraceroute;
use OSCARS::Intradomain::RouteHandler;
use OSCARS::Logger;

my $logger = OSCARS::Logger->new('method_name' => '11_jnxTraceroute.t');
$logger->set_level($NetLogger::INFO);
$logger->set_user_login('testaccount');
$logger->open('/home/oscars/logs/test.log');

my $dbconn = OSCARS::Database->new();
$dbconn->connect('Intradomain');

my $rh = OSCARS::Intradomain::RouteHandler->new('user' => $dbconn);
my $configs = $rh->get_trace_configs();
ok( $configs );
print STDERR "\n";
print STDERR Dumper($configs);

my $test_configs = $rh->get_test_configs('jnxTraceroute');

# Create a traceroute object.
my $jnxTraceroute = OSCARS::Intradomain::JnxTraceroute->new();
ok( $jnxTraceroute );

my $src = $test_configs->{ingress_loopback};
my $dst = $test_configs->{egress_loopback};
print STDERR "\nTraceroute: $src to $dst\n";
$jnxTraceroute->traceroute( $configs, $src, $dst, $logger );
print STDERR "Raw results:\n";
my @rawTracerouteData = $jnxTraceroute->get_raw_hop_data();
ok( @rawTracerouteData );

while(defined($rawTracerouteData[0]))  {
    print STDERR '  ' . $rawTracerouteData[0];
    shift(@rawTracerouteData);
}

print STDERR "Hops:\n";
my @hops = $jnxTraceroute->get_hops();
ok( @hops );

while(defined($hops[0]))  {
    print STDERR "  $hops[0]\n";
    shift(@hops);
}

print STDERR "\n";
$logger->close();
