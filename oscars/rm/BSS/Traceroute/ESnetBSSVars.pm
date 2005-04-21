###################################################################
# Package: ESnetBSSVars.pm
# Author: chin guok (chin@es.net)
#
# Description:
#   Contains variables for the OSCARS scripts.
#
###################################################################

#package ESnetBSSVars;
package BSS::Traceroute::ESnetBSSVars;

use Exporter;
@ISA = qw(Exporter);

# Symbols to export by default.
#@EXPORT = qw();

# Symbols to export on request.
@EXPORT_OK = qw(
  $_jnxTracerouteUser
  $_jnxTracerouteKey
  $_jnxTracerouteSource
);

# Define names for sets of symbols.
%EXPORT_TAGS = (
  JNXTRACEROUTE => [qw(
    $_jnxTracerouteUser
    $_jnxTracerouteKey
    $_jnxTracerouteSource
  )]
);


BEGIN  {

  #####
  #
  # Variables specifically for JnxTraceroute.pm
  #
  #####

  # SSH variable to use for Juniper login.
  $_jnxTracerouteUser = 'traceroute';
  #$_jnxTracerouteKey = '/home/oscars/.ssh/traceroute_id_dsa';
  $_jnxTracerouteKey = '/home/jason/.ssh/traceroute_id_dsa';

  # Default source of tracreoutes.
  $_jnxTracerouteSource = 'chi-cr1.es.net';
}

