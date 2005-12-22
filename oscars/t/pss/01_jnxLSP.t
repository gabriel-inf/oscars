#!/usr/bin/perl

use strict;
use Test::Simple tests => 5;

use OSCARS::PSS::JnxLSP;

use constant LSP_SETUP => 1;
use constant LSP_TEARDOWN => 0;

my $src = '192.168.2.2';
my $dst = '192.168.0.2';
my $lsp_from = 'dev-rt20-e.es.net';
my $lsp_to = '10.0.0.1';

test_lsp($src, $dst, $lsp_from, $lsp_to);

#####
#
# When creating a JnxLSP object, the following parameters can be set:
# - name => string that uniquely identifies a reservation (required)
# - lsp_from => router that will initiate (start-point) LSP (required)
# - lsp_to => router that will terminate LSP (required for setup)
# - bandwidth => LSP bandwidth (e.g. 20, 20k, 20m) (required for setup)
# - lsp_class-of-service => LSP class-of-service (required for setup)
# - lsp_setup-priority => LSP setup priority (optional)
# - lsp_reservation-priority => LSP reservation priority (optional)
# - lsp_description => LSP description (optional)
# - policer_burst-size-limit => LSP burst size limit, typically 10% of
#     bandwidth (required for setup)
# - source-address => IP/network of source (e.g. 10.0.0.1, 10.10.10.0/24)
#     (required for setup)
# - destination-address => destination IP/network of sink
#     (e.g. 10.0.0.1, 10.10.10.0/24) (required for setup)
# - dscp => DSCP value of traffic (optional)
# - protocol => protocol number of traffic (optional)
# - source-port => port number of source traffic (optional)
# - destination-port => port number of destination traffic (optional)
#
#####

##############################################################################
#
sub test_lsp {
    my( $src, $dst, $lsp_from, $lsp_to ) = @_;

    # Initialize LSP information.
    my %lspInfo = (
        'name' => 'oscars_resvID_oscars',
        'lsp_from' => $lsp_from,
        'lsp_to' => $lsp_to,
        'bandwidth' => '10m',
        'lsp_class-of-service' => '4',
        'policer_burst-size-limit' => '1m',
        'source-address' => $src,
        'destination-address' => $dst,
        'dscp' => '4',
        'protocol' => 'udp',
        'source-port' => '5000',
    );

    # Create an LSP object.
    my $jnxLsp = new OSCARS::PSS::JnxLSP(%lspInfo);
    ok($jnxLsp);

    # Setup an LSP.
    print STDERR "Setting up LSP...\n";
    $jnxLsp->configure_lsp(LSP_SETUP);
    my $error = $jnxLsp->get_error();
    ok($error);

    print STDERR "LSP setup complete\n";
    print STDERR "\n";

    # Check that state of the LSP.
    print STDERR "Checking LSP state...  (expected result is 1=>Up)\n";
    my $lspState = $jnxLsp->get_lsp_status();
    $error = $jnxLsp->get_error();
    ok($error);
    print STDERR "LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n";
    print STDERR "\n";

    # Teardown an LSP.
    print STDERR "Tearing down LSP...\n";
    $jnxLsp->configure_lsp(LSP_TEARDOWN);
    $error = $jnxLsp->get_error();
    ok($error);
    print STDERR "LSP teardown complete\n";
    print STDERR "\n";

    # Check that state of the LSP.
    print STDERR "Checking LSP state...  (expected result is -1=>NA)\n";
    $lspState = $jnxLsp->get_lsp_status();
    $error = $jnxLsp->get_error();
    ok($error);
    print STDERR "LSP State: $lspState (-1=>NA, 0=>Down, 1=>Up)\n";
}
