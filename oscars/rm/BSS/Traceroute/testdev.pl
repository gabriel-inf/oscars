#!/usr/bin/perl

use strict;

use BSS::Traceroute::JnxTraceroute;

my ($_error);
my (@_rawTracerouteData) = ();
my (@_hops) = ();


# Create a traceroute object.
my ($_jnxTraceroute) = BSS::Traceroute::JnxTraceroute->new();

print("Traceroute: dev-rt20-e -> 192.160.0.2\n");
$_jnxTraceroute->traceroute("198.128.1.138","192.168.0.2");
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

print("Traceroute: dev-rt20-e -> 192.160.2.2\n");
$_jnxTraceroute->traceroute("198.128.1.138","192.168.2.2");
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
