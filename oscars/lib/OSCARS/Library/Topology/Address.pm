#==============================================================================
package OSCARS::Library::Topology::Address;

=head1 NAME

OSCARS::Library::Topology::Address - conversions to/from host names and IPs

=head1 SYNOPSIS

  use OSCARS::Library::Topology::Address;

=head1 DESCRIPTION

Functionality for host and IP address handling.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# nameToIP:  convert host name to IP address if it isn't already one
# In:   host name or IP, and whether to keep CIDR portion if IP address
# Out:  host IP address
#
sub nameToIP{
    my( $self, $host, $keepCidr ) = @_;

    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)*';
    # if doesn't match IP format, attempt to convert host name to IP address
    if ($host !~ $regexp) { return( inet_ntoa(inet_aton($host)) ); }
    elsif ($keepCidr) { return $host; }
    else { return $1; }   # return IP address without CIDR suffix
} #____________________________________________________________________________


###############################################################################
#
sub ipToName {
    my( $self, $ipaddr ) = @_;

    my $hostname;
    # first group tests for IP address, second handles CIDR blocks
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)';
    # if doesn't match (not CIDR), attempt to get hostname
    if ($ipaddr !~ $regexp) {
        my $ip = inet_aton($ipaddr);
        $hostname = gethostbyaddr($ip, AF_INET);
    }
    if (!$hostname) { $hostname = $ipaddr; }
    return $hostname;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
