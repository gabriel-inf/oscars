#==============================================================================
package OSCARS::Internal::Reservation::Scheduler;

=head1 NAME

OSCARS::Internal::Reservation::Scheduler - Common functionality for scheduling.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Scheduler;

=head1 DESCRIPTION

Functionality common to finding pending reservations and expired reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::Library::Reservation::TimeConversion;
use OSCARS::Library::Reservation::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new();
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
                            'user' => $self->{user}, 'db' => $self->{db});
    # must be overriden
    $self->{opstring} = 'incorrect';
} #____________________________________________________________________________


###############################################################################
# soapMethod:  find reservations to run.  Find all the
#    reservations in db that need to be setup and run in the next N minutes.
#
sub soapMethod {
    my( $self ) = @_;

    my $updateStatus;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    if ($self->{opstring} eq 'setup') { $updateStatus = 'active'; }
    else { $updateStatus = 'finished'; }
    # find reservations that need to be scheduled
    my $reservations =
        $self->getReservations($self->{params}->{timeInterval});
    for my $resv (@$reservations) {
        $self->mapToIPs($resv);
        # call PSS to schedule LSP
        $resv->{lspStatus} = $self->configurePSS($resv);
        $self->{resvLib}->updateReservation( $resv, $updateStatus, 
                                                    $self->{logger} );
    }
    my $results = {};
    $results->{list} = $reservations;
    return $results;
} #____________________________________________________________________________


###############################################################################
# generateMessages:  generate email message
#
sub generateMessages {
    my( $self, $results ) = @_;

    my $reservations = $results->{list};
    if (!@$reservations) {
        return( undef, undef );
    }
    my( @messages );
    my( $subject, $msg );

    for my $resv ( @$reservations ) {
        $resv->{lspConfigTime} = time();
        $resv->{startTime} = $self->{timeLib}->secondsToDatetime(
                                               $resv->{startTime});
        $resv->{endTime} = $self->{timeLib}->secondsToDatetime(
                                               $resv->{endTime});
        $resv->{createdTime} = $self->{timeLib}->secondsToDatetime(
                                               $resv->{createdTime});
        $resv->{lspConfigTime} = $self->{timeLib}->secondsToDatetime(
                                               $resv->{lspConfigTime});
        $subject = "Circuit " . $self->{opstring} . " status for $resv->{login}.";
        $msg =
          "Circuit " . $self->{opstring} .  " status for $resv->{login}, for reservation(s) with parameters:\n";
            # TODO:  if more than one reservation, fix duplicated effort
        $msg .= $self->reservationLspStats( $resv );
        push( @messages, {'msg' => $msg, 'subject' => $subject, 'user' => $resv->{login} } );
    }
    return( \@messages );
} #____________________________________________________________________________


## Private methods

###############################################################################
# configurePSS:  format the args and call pss to do the configuration change
#
sub configurePSS {
    my( $self, $resv ) = @_;   

    my( $error );

    # Create an LSP object.
    my $lsp_info = $self->mapFields($resv);
    $lsp_info->{configs} = $self->{resvLib}->getPssConfigs();
    $lsp_info->{logger} = $self->{logger};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);
    $self->{logger}->info('LSP.' . $self->{opstring}, { 'id' => $resv->{id}  });
    $jnxLsp->configure_lsp($self->{opcode}, $self->{logger});

    if ($error = $jnxLsp->get_error())  { return $error; }
    $self->{logger}->info('LSP.' . $self->{opstring} . '.complete', { 'id' => $resv->{id} });
    return "";
} #____________________________________________________________________________



###############################################################################
#
sub getTimeIntervals {
    my( $self ) = @_;

        # just use defaults for now
    my $statement = "SELECT pollTime FROM configScheduler WHERE id = 1";
    my $row = $self->{db}->getRow( $statement );
    return( $row->{pollTime} );
} #____________________________________________________________________________


###############################################################################
#
sub mapToIPs {
    my( $self, $resv ) = @_;
 
    my $statement = 'SELECT IP FROM hosts WHERE id = ?';
    my $row = $self->{db}->getRow($statement, $resv->{srcHostId});
    $resv->{srcIP} = $row->{IP};
    $row = $self->{db}->getRow($statement, $resv->{destHostId});
    $resv->{destIP} = $row->{IP};

    $statement = 'SELECT loopback FROM topology.routers WHERE id =' .
        ' (SELECT routerId FROM topology.interfaces WHERE topology.interfaces.id = ?)';

    # TODO:  FIX row might be empty
    $row = $self->{db}->getRow($statement, $resv->{ingressInterfaceId});
    $resv->{ingressIP} = $row->{loopback}; 

    $row = $self->{db}->getRow($statement, $resv->{egressInterfaceId});
    $resv->{egressIP} = $row->{loopback}; 
} #____________________________________________________________________________


###############################################################################
#
sub mapFields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{id}",
      'lsp_from' => $resv->{ingressIP},
      'lsp_to' => $resv->{egressIP},
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
    if ($resv->{dscp} && ($resv->{dscp} != 'NULL')) {
        $lsp_info{dscp} = $resv->{dscp};
    }
    if ($resv->{protocol} && ($resv->{protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{protocol};
    }
    return \%lsp_info;
} #____________________________________________________________________________


###############################################################################
# reservationLspStats
#
sub reservationLspStats {
    my( $self, $resv ) = @_;

    my $msg = 
        "Config time:        $resv->{lspConfigTime}\n" .
        "Description:        $resv->{description}\n" .
        "Reservation id:     $resv->{id}\n" .
        "Start time:         $resv->{startTime}\n" .
        "End time:           $resv->{endTime}\n" .
        "Created time:       $resv->{createdTime}\n" .
        "(Times are in UTC $resv->{origTimeZone})\n" .
        "Bandwidth:          $resv->{bandwidth}\n" .
        "Burst limit:        $resv->{burstLimit}\n" .
        "Source:             $resv->{srcIP}\n" .
        "Destination:        $resv->{destIP}\n";
    if ($resv->{srcPort}) {
        $msg .= "Source port:        $resv->{srcPort}\n";
    }
    else { $msg .= "Source port:        DEFAULT\n"; }

    if ($resv->{destPort}) {
        $msg .= "Destination port:   $resv->{destPort}\n";
    }
    else { $msg .= "Destination port:   DEFAULT\n"; }

    if ($resv->{protocol} && ($resv->{protocol} ne 'NULL')) {
        $msg .= "Protocol:           $resv->{protocol}\n";
    }
    else { $msg .= "Protocol:           DEFAULT\n"; }

    if ($resv->{dscp} && ($resv->{dscp} ne 'NU')) {
        $msg .= "DSCP:               $resv->{dscp}\n";
    }
    else { $msg .= "DSCP:               DEFAULT\n"; }

    if ($resv->{class}) {
        $msg .= "Class:              $resv->{class}\n";
    }
    else { $msg .= "Class:   DEFAULT\n"; }

    if ($resv->{ingressRouter}) {
        $msg .= "Ingress loopback:   $resv->{ingressRouter}\n";
    }
    else { $msg .= "Ingress loopback:   $resv->{ingressIP}\n"; }

    if ($resv->{egressRouter}) {
        $msg .= "Egress loopback:    $resv->{egressRouter}\n";
    }
    else { $msg .= "Egress loopback:    $resv->{egressIP}\n"; }
    
    $msg .= "$resv->{lspStatus}\n";
    return $msg;
} #____________________________________________________________________________

                   
######
1;
# vim: et ts=4 sw=4
