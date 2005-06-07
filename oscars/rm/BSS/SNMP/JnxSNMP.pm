##############################################################################
# Package: JnxSNMP.pm
# Authors: chin guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:  Queries MPLS SNMP MIB on Juniper routers.
#####

package BSS::SNMP::JnxSNMP;

use Config::Auto;
use Data::Dumper;
use SNMP;

use strict;

&SNMP::addMibFiles("$ENV{OSCARS_HOME}/BSS/SNMP/mibs/mib-jnx-mpls.txt"};



##################
# Global variables
##################
my (%_lspInfoHash);  # Stores MPLS LSP SNMP data.


################
# Public methods
################

##############################################################################
# new:  create a JnxSNMP.
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
# query_lsp_snmpdata:  query LSP SNMP data from a Juniper router.
# Input:  destination
# Output: <none>
#
sub query_lsp_snmpdata
{
    my ($_self, $_dst) = @_;
    my ($_session, $_vars);
    my ($_varBinds, $_varBindEntry, $_varBindEntryVal);


    # Clear error message.
    $_self->{errMsg} = 0;

    if ( !defined($_dst) )  {
        $_self->{errMsg} = "ERROR: SNMP destination not defined\n";
        return;
    }

    # Initialize SNMP session.
    $_session = new SNMP::Session (
                     'DestHost'       => $_dst,
                     'Community'      => $_self->{config}->{jnx_snmp},
                     'RemotePort'     => 161,
                     'Timeout'        => 300000,  # 300ms
                     'Retries'        => 3,
                     'Version'        => '2c',
                     'UseLongNames'   => 1,       # Return full OID tags
                     'UseNumeric'     => 0,       # Return dotted decimal OID
                     'UseEnums'       => 0,       # Don't use enumerated vals
                     'UseSprintValue' => 1,       # Don't pretty-print values
                   );

    # Set MIB to query.
    $_vars = new SNMP::VarList (['mplsLspList']);  # Repeated variable.

    # Do the bulkwalk of the 0 non-repeaters, and the repeaters.  Ask for no
    # more than 8 values per response packet.  If the caller already knows how
    # many instances will be returned for the repeaters, it can ask only for
    # that many repeaters.
    my (@_response) = $_session->bulkwalk(0, 5, $_vars);

    if ($_session->{ErrorNum})  {
        $_self->{errMsg} = "ERROR: Cannot do bulkwalk: $_session->{ErrorStr} ($_session->{ErrorNum})\n";
        return;
    }

    # Clear lsp information.
    %_lspInfoHash = ();
    $_self->{lsp} = ();

    # Process the results.
    for $_varBinds (@_response)  {

        # Process the returned list of varbinds using the SNMP::Varbind methods.
        for $_varBindEntry (@$_varBinds)  {

            # Place results into the global lspInfoHash.
            # Example of results:
            #   tag: mplsLspName
            #   iid: 111.115.99.97.114.115.95.53.49.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
            #   val:oscars_51
            #   type:OCTETSTR
            #   name:mplsLspName
            $_lspInfoHash{$_varBindEntry->iid}{$_varBindEntry->tag} = $_varBindEntry->val;

            # If the tag is 'mplsLspName', we will create a reference for it in self->{lsp}.
            # e.g.
            #   $_self->{lsp}{oscars_51} = 111.115.99.97.114.115.95.53.49.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
            if ($_varBindEntry->tag eq 'mplsLspName')  {
                $_self->{lsp}{$_varBindEntry->val} = $_varBindEntry->iid;
            }
        }
    }
    return;
}
######

##############################################################################
# get_lsp_info:  returns LSP information retrieved from SNMP query.
# Input: lspName => name of LSP (optional)
#        lspVar => which OID value to return (e.g. "mplsLspState") (optional)
# Output: an array containing the lspName.lspVar and value
#
sub get_lsp_info
{
    my ($_self, $_lspName, $_lspVar) = @_;
    my (@_lspNameArray) = ();
    my (@_lspInfoArray) = ();

    # Figure out which LSP to pull info from.
    # If lspName is not specified, grab all LSPs.
    if (defined($_lspName))  {
        push(@_lspNameArray, $_lspName);
    }
    else  {
        foreach $_lspName (sort keys %{$_self->{lsp}})  {
            push(@_lspNameArray, $_lspName);
        }
    }

    for $_lspName (@_lspNameArray)  {
        if (not(defined($_self->{lsp}{$_lspName})))  {
            $_self->{errMsg} = "ERROR: No such LSP \"$_lspName\"\n";
            return;
        }
        else  {

            # If lspVar is specified, then just select that variable.
            if (defined($_lspVar))  {
                if (not(defined($_lspInfoHash{$_self->{lsp}{$_lspName}}{$_lspVar})))  {
                    $_self->{errMsg} = "ERROR: No such LSP variable \"$_lspName.$_lspVar\"\n";
                    return;
                }
                else  {
                    push(@_lspInfoArray, "$_lspName.$_lspVar", $_lspInfoHash{$_self->{lsp}{$_lspName}}{$_lspVar});
                }
            }
            else  {
                foreach $_lspVar (sort keys %{$_lspInfoHash{$_self->{lsp}{$_lspName}}})  {
                    push(@_lspInfoArray, "$_lspName.$_lspVar", $_lspInfoHash{$_self->{lsp}{$_lspName}}{$_lspVar});
                }
            }
        }
    }
    return(@_lspInfoArray);
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


#################
# Private methods
#################

##############################################################################
# initialize: initialize jnxSNMP with default values if not already
#             populated.
# Input: <none>
# Output: <none>
#
sub initialize 
{
    my ($_self) = @_;

    # read configuration file for this package
    $_self->{config} = Config::Auto::parse($ENV{OSCARS_HOME} . '/oscars.cfg');

    # Clear error message.
    $_self->{errMsg} = 0;

    return();
}
######

1;
