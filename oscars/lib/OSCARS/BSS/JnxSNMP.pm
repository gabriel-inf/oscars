#=============================================================================
package OSCARS::Intradomain::JnxSNMP;

=head1 NAME

OSCARS::Intradomain::JnxSNMP - Queries MPLS SNMP MIB on Juniper routers.

=head1 SYNOPSIS

  use OSCARS::Intradomain::JnxSNMP;

=head1 DESCRIPTION

Queries MPLS SNMP MIB on Juniper routers.  Not currently functional.

=head1 AUTHORS

Chin Guok (chin@es.net),
David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 12, 2006

=cut


use Data::Dumper;
use Net::SNMP;

use strict;


sub new {
    my( $class, %args ) = @_;

    my( $self ) = {%args};
    bless($self, $class);
    $self->initialize();
    return $self;
}

sub initialize {
    my( $self ) = @_;

    # TODO:  fix hard-coded file path
    #&Net::SNMP::addMibFiles(
    #"/home/oscars/mibs/mib-jnx-smi.txt",
    #"/home/oscars/mibs/mib-jnx-mpls.txt"
    #);
} #___________________________________________________________________________


##############################################################################
# initialize_session:  initialize SNMP session, given configs from database and
#                      router.
# Input:  configs, destination
# Output: <none>
#
sub initialize_session {
    my( $self, $configs, $dst ) = @_;

    # Clear error message.
    $self->{errMsg} = 0;
    # Clear lsp information.
    $self->{lspInfo} = {};

    if ( !defined($dst) )  {
        $self->{errMsg} = "ERROR: SNMP destination not defined\n";
        return;
    }
    # Initialize Net::SNMP session.
    my $error;
    ( $self->{session}, $error ) = Net::SNMP->session (
                     -hostname		=> $dst,
                     -port		=> $configs->{snmp_conf_port},
		     -community		=> $configs->{snmp_conf_community},
                     -version		=> $configs->{snmp_conf_version},
                     -timeout		=> $configs->{snmp_conf_timeout},
                     -retries		=> $configs->{snmp_conf_retries},
                   );
    if (!defined($self->{session})) {
        $self->{errMsg} = "ERROR:  Cannot create session: $error\n";
        return;
    }
} #___________________________________________________________________________


##############################################################################
# close_session:  close instance's SNMP session
#
# Input:  <none>
# Output: <none>
#
sub close_session {
    my( $self ) = @_;

    if ($self->{session}) {
        $self->{session}->close();
    }
    return;
} #___________________________________________________________________________


##############################################################################
# query_as_number:  query Juniper router for autonomous service number
#     associated with IP address.
# Input:  IP address
# Output: <none>
#
sub query_as_number {
    my( $self, $ipaddr ) = @_;

    if (!$self->{session}) {
        $self->{errMsg} = "ERROR: Session not initialized\n";
        return;
    }
    # OID is for bgpPeerRemoteAs, concatenated with $ipaddr
    my $oid = '1.3.6.1.2.1.15.3.1.9';
    my $results = $self->{session}->get_request(
		     -varbindlist => ["$oid.$ipaddr"]
    );
    if (!defined($results))  {
	my $err = $self->{session}->error;
        $self->{errMsg} = "ERROR: Cannot make as number query: $err\n";
        return;
    }
    return $results->{"$oid.$ipaddr"};
} #___________________________________________________________________________


##############################################################################
# query_lsp_snmpdata:  query LSP SNMP data from a Juniper router.
# Input:  <none>
# Output: <none>
#
sub query_lsp_snmpdata {
    my( $self ) = @_;

    if (!$self->{session}) {
        $self->{errMsg} = "ERROR: Session not initialized\n";
        return;
    }
    # Do the bulkwalk of the 0 (default) non-repeaters, and the repeaters.  
    # Ask for no more than 8 values per response packet.  If the caller already
    # knows how many instances will be returned for the repeaters, it can ask 
    # only for that many repeaters.
    # TODO:  figure out mpls mib, this oid is for bgpPeerRemoteAs
    my $results = $self->{session}->get_bulk_request(
	             -maxrepetitions => 8,
		     -varbindlist => ["1.3.6.1.2.1.15.3.1.9"]
    );

    if (!defined($results))  {
        $self->{errMsg} = "ERROR: Cannot do bulkwalk: $self->{session}->error\n";
        return;
    }
    if (!defined($self->{session}->var_bind_list)) {
        $self->{errMsg} = "ERROR: No varbindlist: $self->{session}->error\n";
        return;
    }
    $self->{lspInfo} = {};

    # Place results into $self->{lspInfo}.
    # Example of results:
    #   oid: 111.115.99.97.114.115.95.53.49.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0
    #   val:oscars_51
    for my $oid (keys(%{$self->{session}->var_bind_list}))  {
            $self->{lspInfo}->{$oid} = $oid;
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
                if (not(defined($self->{lspInfo}->{$self->{lsp}{$lspName}}{$lspVar})))  {
                    $self->{errMsg} = "ERROR: No such LSP variable \"$lspName.$lspVar\"\n";
                    return;
                }
                else  {
                    push(@lspInfoArray, "$lspName.$lspVar",
                         $self->{lspInfo}->{$self->{lsp}->{$lspName}}{$lspVar});
                }
            }
            else  {
                foreach $lspVar (sort keys %{$self->{lspInfo}->{$self->{lsp}{$lspName}}})  {
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
