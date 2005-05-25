#!/usr/bin/perl

use strict;

use BSS::Traceroute::JnxTraceroute;

#####
#
# Constant definitions.
#
#####


#####
#
# Global variables.
#
#####
my ($_error);
my (@_rawTracerouteData) = ();
my (@_hops) = ();


# Create a traceroute object.
my ($_jnxTraceroute) = BSS::Traceroute::JnxTraceroute->new();

my ($numArgs, $src, $dst);

$numArgs = $#ARGV + 1;
if ($numArgs == 0) {
    $src = "chi-cr1.es.net";
    $dst = "distressed.es.net";
}
elsif ($numArgs == 1) {
    $src = "chi-cr1.es.net";
    $dst = $ARGV[0];
}
elsif ($numArgs == 2) {
    $src = $ARGV[0];
    $dst = $ARGV[1];
}
else {
    print STDERR "Test requires either no or 2 arguments\n";
    exit;
}

print("Traceroute: $src -> $dst\n");

# Run traceroute.
$_jnxTraceroute->traceroute($src, $dst);
if ($_error = $_jnxTraceroute->get_error())  {
  die($_error);
}

print("Raw results:\n");
@_rawTracerouteData = $_jnxTraceroute->get_raw_hop_data();
while(defined($_rawTracerouteData[0]))  {
  print('  ' . $_rawTracerouteData[0]);
  shift(@_rawTracerouteData);
}

print("Hops:\n");
@_hops = $_jnxTraceroute->get_hops();
while(defined($_hops[0]))  {
  print("  $_hops[0]\n");
  shift(@_hops);
}

print("\n");
