#####
#
# Package: JnxTraceroute.pm
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:
#   Execute traceroute on Juniper routers.
#
#####

package BSS::Traceroute::JnxTraceroute;

use Config::Auto;
use Data::Dumper;

use strict;



#####
#
# Constant definitions.
#
#####


#####
#
# Global variables.
#
#####


#####
#
# Public methods
#
#####

#####
#
# Method: new
# Description:
#   Create a JnxTraceroute
# Input: <none>
# Output: new object
#
#####
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

#####
#
# Method: traceroute
# Description:
#   traceroute from a Juniper router.
# Input: source, destination
# Output: <none>
#
#####
sub traceroute
{
  my ($_self, $_src, $_dst) = @_;
  my ($_hopInfo, $_hopCount, $_cmd);

  if ( !defined($_src) || !defined($_dst) )  {
    return "ERROR: Traceroute source or destination not defined\n";
  }

  # Perform the traceroute.
  $_cmd = "ssh -x -a -i $_self->{'jnxConf'}->{'jnx_key'} -l $_self->{'jnxConf'}->{'jnx_user'} $_src traceroute $_dst";
  print STDERR "$_cmd\n";
  if (not(open(_TRACEROUTE_, "$_cmd 2>/dev/null |")))  {
    return "ERROR: Unable to ssh into router and perform traceroute\n";
  }

  # Reset hop information.
  $_hopCount = 0;
  undef($_self->{'rawHopData'});
  undef($_self->{'hops'});

  # Parse the results.
  while ($_hopInfo = <_TRACEROUTE_>)  {
    $_self->{'rawHopData'}[$_hopCount] = $_hopInfo;

    # Get the hop IP address from output.
    #  e.g.
    #  1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
    $_hopInfo =~ m/\((\d+\.\d+\.\d+\.\d+)\)/;
    $_self->{'hops'}[$_hopCount] = $1;

    $_hopCount++;
  }
  return "";
}

#####
#
# Method: get_raw_hop_data
# Description:
#   Returns the raw traceroute data in an array.
# Input: <none>
# Output: Array of raw traceroute data
#         e.g.
#            1  esnet3-lbl3.es.net (198.129.76.26)  0.628 ms  0.569 ms  0.522 ms
#
#####
sub get_raw_hop_data
{
  my ($_self) = @_;

  return(@{$_self->{'rawHopData'}});
}

#####
#
# Method: get_hops
# Description:
#   Returns the IP addresses of the hops in an array.
# Input: <none>
# Output: Array of IP addresses
#
#####
sub get_hops
{
  my ($_self) = @_;

  return(@{$_self->{'hops'}});
}

#####
#
# Private methods
#
#####

#####
#
# Method: initialize
# Description:
#   Initialize jnxTraceroute with default values if not already populated.
# Input: <none>
# Output: <none>
#
#####
sub initialize 
{
  my ($_self) = @_;

  $_self->{'jnxConf'} = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');
}

1;
