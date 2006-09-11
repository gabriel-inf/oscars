#!/usr/local/bin/perl -w
use strict;
########################################################################################################################################
#File      :   path_driver.pl
#Usage     : ./path_driver.pl <topo-file> <latency-file> <source-loopback-ip> <dest-loopback-ip> <length-restriction-on-paths> [xml-output-file]
#Input     : OSPF database dump in juniper xml format, source and destination router loopback ip addresses, and the latency file
#          : The latency file contains one hop latencies with the ip addresses in the inner end point format.
#Output    : Gives the k-paths from the source to the destination with the total latency per path.
#          : If the latency for any of the one-hop paths were unavailable, it would give the total latency
#          : appended with a + sign. At the end of each path there will be a comment starting with a #, which states
#          : which one-hop paths had undetermined latencies. 
#Developer : Neena Kaushik (PhD Candidate, Santa Clara University), Summer Intern, ESnet
#Supervisor: Chin Guok, ESnet
#######################################################################################################################################


########################################### Start of driver program #######################################################

#use library;
use XML::DOM;
use OSCARS::Library::Topology::OptimalPath;

## Define the debugging level: 1 will print all debug statements, 0 will print none
my $debug = 0;
my $level;
my $plist;
my %p_hash;
my %c_hash;


my $topo_file = "null"; 
my $latency_file = "null";
my $source = "null";
my $dest = "null";
my $r_length = "null";
my $output_file = "null";

######################## Start - check the number of arguments passed to the script #######################################

my $num = $#ARGV + 1;
unless ($num >= 5) 
{
    die "Usage : ./path_driver.pl <topo-file> <latency-file> <source-loopback-ip> <dest-loopback-ip> <length-restriction-on-paths> [xml-output-file]";
}

######################## End - check the number of arguments passed to the script ########################################

######################## Start - extract the filename, source and destination ip addresses  #############################

$topo_file = $ARGV[0]; 
$latency_file = $ARGV[1];
$source = $ARGV[2];
$dest = $ARGV[3];
$r_length = $ARGV[4];
if ($num == 6)
{
   $output_file = $ARGV[5];
}

######################## End - extract the filename, source and destination ip addresses  ###############################


######################## Start - Create one copy of topo_doc pointer to be send to the recursive subroutine path #########

my $parser = new XML::DOM::Parser;
my $topo_doc = $parser->parsefile($topo_file);

######################## End - Create one copy of topo_doc pointer to be send to the recursive subroutine path ###########

######################## Start - Create one copy of latency_doc pointer ##################################################

my $lt_parser = new XML::DOM::Parser;
my $latency_doc = $lt_parser->parsefile($latency_file);

######################## End - Create one copy of latency_doc pointer ####################################################

my $currentpath = "";
my $tr = $source; ## Traversed routers to be passed to the path subroutine
$level = 1; ## Level of recursion - For debugging purposes
#$plist = &path($topo_doc, $source, $dest, $currentpath, $level, $tr, $r_length, $debug);
my $op  = OSCARS::Library::Topology::OptimalPath->new();
$plist = $op->path($topo_doc, $source, $dest, $currentpath, $level, $tr, $r_length, $debug);


## Print the list of paths obtained in a easily readable form
$op->pretty_printer($source, $dest, $plist, $debug);

## Print the list of paths obtained in a easily readable form with total latency per path and one hops with undetermined latencies
$plist = $op->pretty_printer_with_latency($source, $dest, $plist, $latency_doc, $topo_doc, $debug);

## Convert the pathlist to hash form
%p_hash = $op->path_hash($plist, $debug);

## A Sample of printing the hash data structure 
$op->hash_path_print($source, $dest, $debug, %p_hash);

## Convert the pathlist to xml form
if ($output_file eq "null") {}
else
{
    $op->create_path_xml_file($source, $dest, $output_file, $debug, %p_hash);
}

$topo_doc->dispose; ## Clear up the memory being held by the topo_doc pointer
$latency_doc->dispose; ## Clear up the memory being held by the topo_doc pointer

########################################### End of driver program ####################################################


