#==============================================================================
package OSCARS::Internal::Reservation::Scheduler;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

=head1 NAME

OSCARS::Internal::Reservation::Scheduler - Common functionality for scheduling.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Scheduler;

=head1 DESCRIPTION

Functionality common to finding pending reservations and expired reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::PSS::JnxLSP;
use OSCARS::Library::Reservation;
use OSCARS::Library::Topology::Host;
use OSCARS::Library::Topology::Path;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{reservation} = OSCARS::Library::Reservation->new(
                            'user' => $self->{user}, 'db' => $self->{db});
    $self->{host} = OSCARS::Library::Topology::Host->new(
                                                     'db' => $self->{db});
    $self->{path} = OSCARS::Library::Topology::Path->new(
                                                     'db' => $self->{db});
    # must be overriden
    $self->{opstring} = 'incorrect';
} #____________________________________________________________________________


###############################################################################
# soapMethod:  handles setting up and tearing down LSP's.
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $updateStatus;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    $request->{timeInterval} = 20;  #TODO:  FIX explicit setting
    if ($self->{opstring} eq 'setup') { $updateStatus = 'active'; }
    else { $updateStatus = 'finished'; }
    # find reservations that need to be scheduled
    my $reservations =
        $self->getReservations($request->{timeInterval});
    my @formattedList = ();
    for my $resv (@$reservations) {
        $self->mapToIPs($resv);
        # call PSS to schedule LSP
        $resv->{lspStatus} = $self->configurePSS($resv, $logger);
        $self->{reservation}->update( $resv, $updateStatus );
        push( @formattedList, $self->{reservation}->format($resv) );
    }
    my $response = \@formattedList;
    return $response;
} #____________________________________________________________________________


## Private methods

###############################################################################
# configurePSS:  format the args and call pss to do the configuration change
#
sub configurePSS {
    my( $self, $resv, $logger ) = @_;   

    my( $error );

    # Create an LSP object.
    my $lsp_info = $self->mapFields($resv);
    $lsp_info->{logger} = $logger;
    $lsp_info->{db} = $self->{db};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);
    $logger->info('LSP.' . $self->{opstring}, { 'id' => $resv->{id}  });
    $jnxLsp->configure_lsp($self->{opcode}, $logger);

    if ($error = $jnxLsp->get_error())  { return $error; }
    $logger->info('LSP.' . $self->{opstring} . '.complete',
                  { 'id' => $resv->{id} });
    return "";
} #____________________________________________________________________________



###############################################################################
#
sub mapToIPs {
    my( $self, $resv ) = @_;
 
    $resv->{srcIP} = $self->{host}->nameToIP( $resv->{srcHost} );
    $resv->{destIP} = $self->{host}->nameToIP( $resv->{destHost} );
    my $addresses = $self->{path}->addresses( $resv->{pathId}, 'loopback' );
    my @hopInfo = @{$addresses};
    for my $hop ( @hopInfo ) {
        if ( $hop->{description} eq 'loopback' ) {
            $resv->{ingressLoopbackIP} = $hop->{IP};
            last;
        }
    }
    $resv->{egressLoopbackIP} = $hopInfo[-1]->{IP};
} #____________________________________________________________________________


###############################################################################
#
sub mapFields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{id}",
      'lsp_from' => $resv->{ingressLoopbackIP},
      'lsp_to' => $resv->{egressLoopbackIP},
      'bandwidth' => $resv->{bandwidth},
      'lsp_class-of-service' => $resv->{class},
      'policer_burst-size-limit' =>  $resv->{burstLimit},
      'source-address' => $resv->{srcIP},
      'destination-address' => $resv->{destIP},
    );
    if ($resv->{srcPort} && ($resv->{srcPort} != 'NULL')) {
        $lsp_info{'source-port'} = $resv->{srcPort};
    }
    if ($resv->{destPort} && ($resv->{destPort} != 'NULL')) {
        $lsp_info{'destination-port'} = $resv->{destPort};
    }
    #if ($resv->{dscp} && ($resv->{dscp} != 'NULL')) {
    #$lsp_info{dscp} = $resv->{dscp};
    #}
    if ($resv->{protocol} && ($resv->{protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{protocol};
    }
    return \%lsp_info;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
