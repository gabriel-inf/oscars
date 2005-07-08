##############################################################################
# Package: JnxTraceroute.pm
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:  Execute traceroute on Juniper routers.
#####

package BSS::Traceroute::JnxTraceroute;

use Config::Auto;
use Data::Dumper;

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
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};

    # Bless $_self into designated class.
    bless($_self, $_class);

    # Initialize.
    $_self->initialize();
    return($_self);
}
######

##############################################################################
# traceroute:  traceroute from a Juniper router.
# Input:  source, destination
# Output: <none>
#
sub traceroute
{
    my ($_self, $_src, $_dst) = @_;
    my ($_hopInfo, $_hopCount, $_cmd);

    # Clear error message.
    $_self->{errMsg} = 0;
    if ( !defined($_src) || !defined($_dst) )  {
        $_self->{errMsg} = "ERROR: Traceroute source or destination not defined\n";
        return(0);
    }

    # Perform the traceroute.
    $_cmd = "ssh -x -a -i $_self->{config}->{jnx_key} -l $_self->{config}->{jnx_user} $_src traceroute $_dst wait 1";
    print STDERR "$_cmd\n";
    if (not(open(_TRACEROUTE_, "$_cmd 2>/dev/null |")))  {
        $_self->{errMsg} = "ERROR: Unable to ssh into router and perform traceroute\n";
        return(0);
    }

    # Reset hop information.
    $_hopCount = 0;
    undef($_self->{rawHopData});
    undef($_self->{hops});

    # Parse the results.
    while ($_hopInfo = <_TRACEROUTE_>)  {
        $_self->{rawHopData}[$_hopCount] = $_hopInfo;

        # Get the hop IP address from output, e.g.
        #  1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
        next if ($_hopInfo !~ m/\((\d+\.\d+\.\d+\.\d+)\)/);
        $_self->{hops}[$_hopCount] = $1;
        $_hopCount++;
    }
    return(1);
}
######

##############################################################################
# get_error:  Return the error message (0 if none).
# In:  <none>
# Out: Error message
#
sub get_error {
    my ($_self) = @_;

    return($_self->{errMsg});
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
    my ($_self) = @_;

    return(@{$_self->{rawHopData}});
}
######

##############################################################################
# get_hops:  returns the IP addresses of the hops in an array.
# Input:  <none>
# Output: array of IP addresses
#
sub get_hops
{
    my ($_self) = @_;

    return(@{$_self->{hops}});
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
    my ($_self) = @_;

    $_self->{config} = Config::Auto::parse($ENV{OSCARS_HOME} . '/oscars.cfg');
    # clear error message
    $_self->{errMsg} = 0;
}
######

1;
