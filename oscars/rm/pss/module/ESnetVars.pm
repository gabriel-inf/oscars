###################################################################
# Package: esnet-vars.pm
# Author: chin guok (chin@es.net)
#
# Description:
#   Contains variables for the OSCARS scripts.
#
###################################################################

package ESnetVars;

use Exporter;
@ISA = qw(Exporter);

# Symbols to export by default.
#@EXPORT = qw();

# Symbols to export on request.
@EXPORT_OK = qw(
  $_jnxLspAccess
  $_jnxLspLogin
  $_jnxLspPassword
  $_jnxLspSetupXmlFile
  $_jnxLspTeardownXmlFile
  $_jnxLspExtIfFilter
  $_jnxLspFirewallFilterMaker
  $_jnxLspCoS
  $_jnxLspSetupPriority
  $_jnxLspResvPriority
);

# Define names for sets of symbols.
%EXPORT_TAGS = (
  JNXLSP => [qw(
    $_jnxLspAccess
    $_jnxLspLogin
    $_jnxLspPassword
    $_jnxLspSetupXmlFile
    $_jnxLspTeardownXmlFile
    $_jnxLspExtIfFilter
    $_jnxLspFirewallFilterMaker
    $_jnxLspCoS
    $_jnxLspSetupPriority
    $_jnxLspResvPriority
  )]
);


BEGIN  {

  #####
  #
  # Variables specifically for jnxLSP.pm
  #
  #####

  # Router access.
  $_jnxLspAccess = 'ssl';
  $_jnxLspLogin = 'junoscript';
  $_jnxLspPassword = 'JUNOScript';

  # XML related variables.
  $_jnxLspSetupXmlFile = 'xml/ESnet-OSCARS_Juniper_LSP-setup.xml';
  $_jnxLspTeardownXmlFile = 'xml/ESnet-OSCARS_Juniper_LSP-teardown.xml';
  $_jnxLspExtIfFilter = 'test-filter';
  $_jnxLspFirewallFilterMaker = 'oscars-filters-start';

  # LSP default values.
  $_jnxLspCoS = 4;  # EF
  $_jnxLspSetupPriority = 4;
  $_jnxLspResvPriority = 4;
}

