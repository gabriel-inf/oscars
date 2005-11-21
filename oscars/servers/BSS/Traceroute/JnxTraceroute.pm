###############################################################################
package BSS::Traceroute::JnxTraceroute;

# Executes traceroute on Juniper routers.
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Last Modified:  November 21, 2005

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
    my ($self, $configs, $src, $dst) = @_;
    my ($hopInfo, $hopCount, $cmd);

    # Clear error message.
    if ( !defined($src) || !defined($dst) )  {
        throw Error::Simple("Traceroute source or destination not defined.");
    }

    # Remove subnet mask if necessary.
    # e.g. 10.0.0.0/8 => 10.0.0.0
    $dst =~ s/\/\d*$//;

    # Perform the traceroute.
    $cmd = "ssh -x -a -i $configs->{trace_conf_jnx_key} -l " .
           "$configs->{trace_conf_jnx_user} $src traceroute $dst wait " .
           "$configs->{trace_conf_timeout} ttl " .
           "$configs->{trace_conf_ttl}";
    print STDERR "$cmd\n";
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

        # Get the hop IP address from output, e.g.
        #  1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
        next if ($hopInfo !~ m/\((\d+\.\d+\.\d+\.\d+)\)/);
        $self->{hops}[$hopCount] = $1;
        $hopCount++;
    }
    close(_TRACEROUTE_);
    return 1;
} #____________________________________________________________________________ 


###############################################################################
# get_raw_hop_data:  returns the raw traceroute data in an array.
# Input: <none>
# Output: Array of raw traceroute data, e.g.
#         1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
#
sub get_raw_hop_data
{
    my ($self) = @_;

    return @{$self->{rawHopData}};
} #____________________________________________________________________________ 


##############################################################################
# get_hops:  returns the IP addresses of the hops in an array.
# Input:  <none>
# Output: array of IP addresses
#
sub get_hops
{
    my ($self) = @_;

    return @{$self->{hops}};
} #____________________________________________________________________________ 


######
1;
