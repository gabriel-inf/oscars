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

print("Traceroute: chi-cr1.es.net -> distressed.es.net\n");

# Traceroute to distressed.es.net (from default source (chi-cr1.es.net)).
$_jnxTraceroute->traceroute("distressed.es.net");
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
print("Traceroute: aoa-cr1.es.net -> distressed.es.net\n");

# Traceroute to distressed.es.net (from default source (chi-cr1.es.net)).
$_jnxTraceroute->traceroute("aoa-cr1.es.net", "distressed.es.net");

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

