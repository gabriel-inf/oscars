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
  my ($_self, $_addr1, $_addr2) = @_;
  my ($_source, $_destination);
  my ($_hopInfo, $_hopCount);

  # Clear error message.
  $_self->{'jnxConf'}->{'errMsg'} = 0;

  # If only _addr1 is defined, then
  #   _source = $_self{"source"}, _destination = _addr1.
  # If both _addr1 AND _addr2 are defined, then
  #   _source = $_addr1, _destination = _addr2
  if (defined($_addr1))  {
    if (defined($_addr2))  {
      $_source = $_addr1;
      $_destination = $_addr2;
    }
    else  {
      $_source = $_self->{'jnxConf'}->{'source'};
      $_destination = $_addr1;
    }
  }
  else  {
    $_self->{'jnxConf'}->{'errMsg'} = "ERROR: Traceroute destination not defined\n";
    return();
  }

  # Perform the traceroute.
  if (not(open(_TRACEROUTE_, "ssh -x -a -i $_self->{'jnxConf'}->{'key'} -l $_self->{'jnxConf'}->{'user'} $_source traceroute $_destination 2>/dev/null |")))  {
    $_self->{'jnxConf'}->{'errMsg'} = "ERROR: Unable to ssh into router and perform traceroute\n";
    return();
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
  return();
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
# Method: get_error
# Description:
#   Return the error message (0 if none).
# Input: <none>
# Output: Error message
#
#####
sub get_error
{
  my ($_self) = @_;

  return($_self->{'jnxConf'}->{'errMsg'});
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

  $_self->{'jnxConf'} = Config::Auto::parse('JnxTraceroute.config');

  # Clear error message.
  $_self->{'jnxConf'}->{'errMsg'} = 0;

  # default router to traceroute from
  $_self->{'defaultrouter'} = $_self->{'jnxConf'}->{'source'};

  return();
}

1;
