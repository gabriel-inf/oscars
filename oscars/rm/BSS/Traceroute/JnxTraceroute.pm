##############################################################################
# Package: JnxTraceroute.pm
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:  Execute traceroute on Juniper routers.
#####

package BSS::Traceroute::JnxTraceroute;

use Config::Auto;
use Data::Dumper;
use Error qw(:try);

use Common::Exception;

use strict;


################
# Public methods
################

##############################################################################
# new:  create a JnxTraceroute.
# Input:  <none>
# Output: new object
#
sub new
{
    my ($class, %args) = @_;
    my ($self) = {%args};

    # Bless $self into designated class.
    bless($self, $class);

    # Initialize.
    $self->initialize();
    return($self);
}
######

##############################################################################
# traceroute:  traceroute from a Juniper router.
# Input:  source, destination
# Output: <none>
#
sub traceroute
{
    my ($self, $src, $dst) = @_;
    my ($hopInfo, $hopCount, $cmd);

    # Clear error message.
    if ( !defined($src) || !defined($dst) )  {
        throw Common::Exception("Traceroute source or destination not defined.");
    }

    # Perform the traceroute.
    $cmd = "ssh -x -a -i $self->{config}->{jnx_key} -l $self->{config}->{jnx_user} $src traceroute $dst wait 1";
    print STDERR "$cmd\n";
    if (not(open(_TRACEROUTE_, "$cmd 2>/dev/null |")))  {
        throw Common::Exception("Unable to ssh into router and perform traceroute.");
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
    return(1);
}
######

##############################################################################
# get_raw_hop_data:  returns the raw traceroute data in an array.
# Input: <none>
# Output: Array of raw traceroute data, e.g.
#         1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
#
sub get_raw_hop_data
{
    my ($self) = @_;

    return(@{$self->{rawHopData}});
}
######

##############################################################################
# get_hops:  returns the IP addresses of the hops in an array.
# Input:  <none>
# Output: array of IP addresses
#
sub get_hops
{
    my ($self) = @_;

    return(@{$self->{hops}});
}
######

#################
# Private methods
#################

##############################################################################
# initialize: initialize jnxTraceroute with default values if not already
#             populated.
# Input: <none>
# Output: <none>
#
sub initialize 
{
    my ($self) = @_;

    $self->{config} = Config::Auto::parse($ENV{OSCARS_HOME} . '/oscars.cfg');
}
######

1;
