#==============================================================================
package OSCARS::Library::Topology::JnxTraceroute;

=head1 NAME

OSCARS::Library::Topology::JnxTraceroute - Executes traceroute on Juniper routers.

=head1 SYNOPSIS

  use OSCARS::Library::Topology::JnxTraceroute;

=head1 DESCRIPTION

Executes traceroute on Juniper routers.

=head1 AUTHORS

Chin Guok (chin@es.net),
David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 4, 2006

=cut


use Data::Dumper;
use Error qw(:try);

use strict;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# traceroute:  traceroute from a Juniper router.
# Input:  source, destination
# Output: <none>
#
sub traceroute
{
    my ($self, $configs, $src, $dst, $logger) = @_;
    my ($hopInfo, $hopCount, $cmd);

    # Clear error message.
    if ( !defined($src) || !defined($dst) )  {
        throw Error::Simple("Traceroute source or destination not defined.");
    }

    # Remove subnet mask if necessary.
    # e.g. 10.0.0.0/8 => 10.0.0.0
    $dst =~ s/\/\d*$//;

    # Perform the traceroute.
    $cmd = "ssh -x -a -i $configs->{jnxKey} -l " .
           "$configs->{jnxUser} $src traceroute $dst wait " .
           "$configs->{timeout} ttl " .
           "$configs->{ttl}";
    $logger->info('traceroute.ssh',
	    {'command' => $cmd, 'src' => $src, 'dst' => $dst});
    if (not(open(_TRACEROUTE_, "$cmd 2>/dev/null |")))  {
        throw Error::Simple("Unable to ssh into router and perform traceroute.");
    }

    # Reset hop information.
    $hopCount = 0;
    undef($self->{rawHopData});
    undef($self->{hops});

    # Parse the results.
    while ($hopInfo = <_TRACEROUTE_>)  {
        $self->{rawHopData}[$hopCount] = $hopInfo;
	$logger->info('traceroute.raw', { 'hop' => substr($hopInfo, 0, -1) });

        # Get the hop IP address from output, e.g.
        #  1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
        next if ($hopInfo !~ m/\((\d+\.\d+\.\d+\.\d+)\)/);
        $self->{hops}[$hopCount] = $1;
	$logger->info('traceroute', { 'hop' => substr($1, 0, -1) });
        $hopCount++;
    }
    close(_TRACEROUTE_);
    return 1;
} #____________________________________________________________________________


###############################################################################
# getRawHopData:  returns the raw traceroute data in an array.
# Input: <none>
# Output: Array of raw traceroute data, e.g.
#         1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
#
sub getRawHopData
{
    my ($self) = @_;

    return @{$self->{rawHopData}};
} #____________________________________________________________________________


##############################################################################
# getHops:  returns the IP addresses of the hops in an array.
# Input:  <none>
# Output: array of IP addresses
#
sub getHops
{
    my ($self) = @_;

    return @{$self->{hops}};
} #____________________________________________________________________________


######
1;
