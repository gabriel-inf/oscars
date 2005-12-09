#!/usr/bin/perl

use strict;
use Test::Simple tests => 5;

use OSCARS::AAAS::Logger;
use OSCARS::BSS::Database;
use OSCARS::BSS::JnxTraceroute;

my $logger = OSCARS::AAAS::Logger->new( 'dir' => '/home/oscars/logs',
                                        'method' => 'test');
ok($logger);
$logger->start_log();

# Create a traceroute object.
my $jnxTraceroute = OSCARS::BSS::JnxTraceroute->new();
ok($jnxTraceroute);

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);

my $configs = $dbconn->get_trace_configs();
ok($configs);
my $src = 'chi-cr1.es.net';
my $dst = 'distressed.es.net';
my( $status, $msg ) = 
        test_traceroute( $jnxTraceroute, $configs, $src, $dst, $logger );
ok( $status, $msg );
print STDERR $msg;
$logger->end_log();

##############################################################################
#
sub test_traceroute {
    my( $jnxTraceroute, $configs, $src, $dst, $logger ) = @_;

    my $msg = "\nTraceroute: chi-cr1.es.net to distressed.es.net\n";
    # Run traceroute.
    $jnxTraceroute->traceroute($configs, $src, $dst, $logger);

    $msg .= "Raw results:\n";
    my @rawTracerouteData = $jnxTraceroute->get_raw_hop_data();
    while(defined($rawTracerouteData[0]))  {
        $msg .= '  ' . $rawTracerouteData[0];
        shift(@rawTracerouteData);
    }

    $msg .= "Hops:\n";
    my @hops = $jnxTraceroute->get_hops();
    while(defined($hops[0]))  {
        $msg .= "  $hops[0]\n";
        shift(@hops);
    }
    $msg .= "\n";
    return( 1, $msg );
}
