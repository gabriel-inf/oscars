#==============================================================================
package OSCARS::Public::Intradomain::SchedulerCommon;

=head1 NAME

OSCARS::Public::Intradomain::SchedulerCommon - Common functionality for reservation scheduling.

=head1 SYNOPSIS

  use OSCARS::Public::Intradomain::SchedulerCommon;

=head1 DESCRIPTION

Functionality common to finding pending reservations and expired reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 

###############################################################################
#
sub getTimeIntervals {
    my( $self ) = @_;

        # just use defaults for now
    my $statement = "SELECT pollTime FROM schedulerConfs WHERE id = 1";
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

    $statement = 'SELECT loopback FROM routers WHERE id =' .
        ' (SELECT routerId FROM interfaces WHERE interfaces.id = ?)';

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
