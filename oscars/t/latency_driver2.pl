#!/usr/local/bin/perl -w

use strict;
use OSCARS::Library::Topology::OptimalPath;

########################################################################################################################################
#File      :   latency_driver.pl
#Usage     : ./latency_driver.pl <latency-input-file> <latency-output-file> <topology-file>
#Input     : Latency file in xml form with data generated from pinger and traceroute
#Output    : Latency file in xml form with latencies in both forward and reverse directions 
#            with links in inner endpoint format
#Developer : Neena Kaushik (PhD Candidate, Santa Clara University), Summer Intern, ESnet
#Supervisor: Chin Guok, ESnet
#######################################################################################################################################

## Start of driver program

## Define the debugging level: 1 will print all debug statements, 0 will print none
my $debug = 0;
my $parser = "";

## Start -Check the number of arguments passed to the script

my $num = $#ARGV + 1;
unless ($num == 3)
{
    die "Usage : ./latency_driver.pl <latency-input-file> <latency-output-file> <topology-file>";
}

## End -Check the number of arguments passed to the script

## Start -extract the input filename, output filename and topology 

my $lt_file = $ARGV[0];
my $out_file = $ARGV[1];
my $topo_file = $ARGV[2];

## End -extract the input filename, output filename and topology 


$parser = new XML::DOM::Parser;
my $topo_doc = $parser->parsefile($topo_file);

$parser = new XML::DOM::Parser;
my $lt_doc = $parser->parsefile($lt_file);

my $op  = OSCARS::Library::Topology::OptimalPath->new();
$op->create_file($lt_doc, $out_file, $topo_doc, $debug);

$lt_doc->dispose;
$topo_doc->dispose;

## End of driver program


