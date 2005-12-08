##############################################################################
package BSS::SNMP::JnxSNMP;

# Authors: Chin Guok (chin@es.net), David Robertson (dwrobertson@lbl.gov)
# Description:  Queries MPLS SNMP MIB on Juniper routers.
# Last modified:  December 7, 2005


use Data::Dumper;
use SNMP;

use strict;

&SNMP::addMibFiles(
  "$ENV{OSCARS_HOME}/BSS/SNMP/mibs/mib-jnx-smi.txt",
  "$ENV{OSCARS_HOME}/BSS/SNMP/mibs/mib-jnx-mpls.txt"
);


sub new {
    my( $class, %args ) = @_;

    my( $self ) = {%args};
    bless($self, $class);
    return $self;
} #___________________________________________________________________________


##############################################################################
# query_lsp_snmpdata:  query LSP SNMP data from a Juniper router.
# Input:  destination
# Output: <none>
#
sub query_lsp_snmpdata {
    my( $self, $configs, $dst ) = @_;

    # Clear error message.
    $self->{errMsg} = 0;
    # Clear lsp information.
    $self->{lspInfo} = ();

    if ( !defined($dst) )  {
        $self->{errMsg} = "ERROR: SNMP destination not defined\n";
        return;
    }

    # Initialize SNMP session.
    my $session = new SNMP::Session (
                     'DestHost'       => $dst,
                     'Community'      => $configs->{jnx_snmp},
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
    my $vars = new SNMP::VarList (['mplsLspList']);  # Repeated variable.

    # Do the bulkwalk of the 0 non-repeaters, and the repeaters.  Ask for no
    # more than 8 values per response packet.  If the caller already knows how
    # many instances will be returned for the repeaters, it can ask only for
    # that many repeaters.
    my @response = $session->bulkwalk(0, 5, $vars);

    if ($session->{ErrorNum})  {
        $self->{errMsg} = "ERROR: Cannot do bulkwalk: $session->{ErrorStr} ($session->{ErrorNum})\n";
        return;
    }
    $self->{lsp} = ();

    # Process the results.
    for my $varBinds (@response)  {

        # Process the returned list of varbinds using the SNMP::Varbind methods.
        for my $varBindEntry (@$varBinds)  {

            # Place results into lspInfo.
            # Example of results:
            #   tag: mplsLspName
            #   iid: 111.115.99.97.114.115.95.53.49.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
            #   val:oscars_51
            #   type:OCTETSTR
            #   name:mplsLspName
            $self->{lspInfo}{$varBindEntry->{iid}{$varBindEntry->{tag}} = $varBindEntry->{val};

            # If the tag is 'mplsLspName', we will create a reference for it in self->{lsp}.
            # e.g.
            #   $self->{lsp}{oscars_51} = 111.115.99.97.114.115.95.53.49.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
            if ($varBindEntry->{tag} eq 'mplsLspName')  {
                $self->{lsp}{$varBindEntry->{val}} = $varBindEntry->{iid};
            }
        }
    }
    return;
} #___________________________________________________________________________


##############################################################################
# get_lsp_info:  returns LSP information retrieved from SNMP query.
# Input: lspName => name of LSP (optional)
#        lspVar => which OID value to return (e.g. "mplsLspState") (optional)
# Output: an array containing the lspName.lspVar and value
#
sub get_lsp_info {
    my( $self, $lspName, $lspVar ) = @_;

    my @lspNameArray = ();
    my @lspInfoArray = ();

    # Figure out which LSP to pull info from.
    # If lspName is not specified, grab all LSPs.
    if (defined($lspName))  {
        push(@lspNameArray, $lspName);
    }
    else  {
        foreach $lspName (sort keys %{$self->{lsp}})  {
            push(@lspNameArray, $lspName);
        }
    }

    for $lspName (@lspNameArray)  {
        if (not(defined($self->{lsp}{$lspName})))  {
            $self->{errMsg} = "ERROR: No such LSP \"$lspName\"\n";
            return;
        }
        else  {

            # If lspVar is specified, then just select that variable.
            if (defined($lspVar))  {
                if (not(defined($self->{lspInfo}{$self->{lsp}{$lspName}}{$lspVar})))  {
                    $self->{errMsg} = "ERROR: No such LSP variable \"$lspName.$lspVar\"\n";
                    return;
                }
                else  {
                    push(@lspInfoArray, "$lspName.$lspVar", $self->{lspInfo}{$self->{lsp}{$lspName}}{$lspVar});
                }
            }
            else  {
                foreach $lspVar (sort keys %{$self->{lspInfo}{$self->{lsp}{$lspName}}})  {
                    push(@lspInfoArray, "$lspName.$lspVar", $self->{lspInfo}{$self->{lsp}{$lspName}}{$lspVar});
                }
            }
        }
    }
    return \@lspInfoArray;
} #___________________________________________________________________________


##############################################################################
# get_error:  Return the error message (0 if none).
# In:  <none>
# Out: Error message
#
sub get_error {
    my( $self ) = @_;

    return $self->{errMsg};
} #___________________________________________________________________________


######
1;
