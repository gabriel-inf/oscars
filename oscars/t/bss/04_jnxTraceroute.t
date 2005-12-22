#!/usr/bin/perl

use strict;
use Test::Simple tests => 5;

use OSCARS::Logger;
use OSCARS::Database;
use OSCARS::BSS::JnxTraceroute;

my $logger = OSCARS::Logger->new( 'dir' => '/home/oscars/logs',
                                  'method' => 'test');
ok($logger);

# Create a traceroute object.
my $jnxTraceroute = OSCARS::BSS::JnxTraceroute->new();
ok($jnxTraceroute);

my $dbconn = OSCARS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);
$dbconn->connect('BSS');

my $configs = get_trace_configs($dbconn);
ok($configs);
my $src = 'nettrash3.es.net';
my $dst = 'dc-cr1.es.net';
my( $status, $msg ) = 
        test_traceroute( $jnxTraceroute, $configs, $src, $dst, $logger );
ok( $status, $msg );
print STDERR $msg;
$logger->end_log('test', '');

##############################################################################
#
sub test_traceroute {
    my( $jnxTraceroute, $configs, $src, $dst, $logger ) = @_;

    my $msg = "\nTraceroute: nettrash3.es.net to dc-cr1.es.net\n";
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
} #____________________________________________________________________________


###############################################################################
# Adapted from method in OSCARS::BSS::RouteHandler
#
sub get_trace_configs {
    my( $dbconn ) = @_;

        # use default for now
    my $statement = "SELECT " .
            "trace_conf_jnx_source, trace_conf_jnx_user, trace_conf_jnx_key, " .
            "trace_conf_ttl, trace_conf_timeout, " .
            "trace_conf_run_trace, trace_conf_use_system, " .
            "trace_conf_use_ping "  .
            "FROM trace_confs where trace_conf_id = 1";
    my $configs = $dbconn->get_row($statement);
    return $configs;
} #____________________________________________________________________________
