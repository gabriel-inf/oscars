#==============================================================================
package OSCARS::Intradomain::SchedulerCommon;

=head1 NAME

OSCARS::Intradomain::SchedulerCommon - Common functionality for reservation scheduling.

=head1 SYNOPSIS

  use OSCARS::Intradomain::SchedulerCommon;

=head1 DESCRIPTION

Functionality common to finding pending reservations and expired reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

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
sub get_time_intervals {
    my( $self ) = @_;

        # just use defaults for now
    my $statement = "SELECT scheduler_db_poll_time, scheduler_time_interval" .
             " FROM Intradomain.scheduler_confs WHERE scheduler_conf_id = 1";
    my $row = $self->{db}->get_row( $statement );
    return( $row->{scheduler_db_poll_time}, $row->{scheduler_time_interval} );
} #____________________________________________________________________________


###############################################################################
#
sub map_to_ips {
    my( $self, $resv ) = @_;
 
    my $statement = 'SELECT host_ip FROM Intradomain.hosts WHERE host_id = ?';
    my $row = $self->{db}->get_row($statement, $resv->{src_host_id});
    $resv->{source_ip} = $row->{host_ip};
    $row = $self->{db}->get_row($statement, $resv->{dst_host_id});
    $resv->{destination_ip} = $row->{host_ip};

    $statement = 'SELECT router_loopback FROM Intradomain.routers' .
                ' WHERE router_id =' .
                ' (SELECT router_id FROM Intradomain.interfaces' .
                '  WHERE interface_id = ?)';

    # TODO:  FIX row might be empty
    $row = $self->{db}->get_row($statement, $resv->{ingress_interface_id});
    $resv->{ingress_ip} = $row->{router_loopback}; 

    $row = $self->{db}->get_row($statement, $resv->{egress_interface_id});
    $resv->{egress_ip} = $row->{router_loopback}; 
} #____________________________________________________________________________


###############################################################################
#
sub map_fields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{reservation_id}",
      'lsp_from' => $resv->{ingress_ip},
      'lsp_to' => $resv->{egress_ip},
      'bandwidth' => $resv->{reservation_bandwidth},
      'lsp_class-of-service' => $resv->{reservation_class},
      'policer_burst-size-limit' =>  $resv->{reservation_burst_limit},
      'source-address' => $resv->{source_ip},
      'destination-address' => $resv->{destination_ip},
    );
    if ($resv->{reservation_src_port} &&
        ($resv->{reservation_src_port} != 'NULL')) {
        $lsp_info{'source-port'} = $resv->{reservation_src_port};
    }
    if ($resv->{reservation_dst_port} &&
      ($resv->{reservation_dst_port} != 'NULL')) {
        $lsp_info{'destination-port'} = $resv->{reservation_dst_port};
    }
    if ($resv->{reservation_dscp} &&
      ($resv->{reservation_dscp} != 'NULL')) {
        $lsp_info{dscp} = $resv->{reservation_dscp};
    }
    if ($resv->{reservation_protocol} &&
      ($resv->{reservation_protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{reservation_protocol};
    }
    return \%lsp_info;
} #____________________________________________________________________________


###############################################################################
# reservation_lsp_stats
#
sub reservation_lsp_stats {
    my( $self, $resv ) = @_;

    my $msg = 
        "Config time:        $resv->{lsp_config_time}\n" .
        "Description:        $resv->{reservation_description}\n" .
        "Reservation id:     $resv->{reservation_id}\n" .
        "Start time:         $resv->{reservation_start_time}\n" .
        "End time:           $resv->{reservation_end_time}\n" .
        "Created time:       $resv->{reservation_created_time}\n" .
        "(Times are in UTC $resv->{reservation_time_zone})\n" .
        "Bandwidth:          $resv->{reservation_bandwidth}\n" .
        "Burst limit:        $resv->{reservation_burst_limit}\n" .
        "Source:             $resv->{source_ip}\n" .
        "Destination:        $resv->{destination_ip}\n";
    if ($resv->{reservation_src_port}) {
        $msg .= "Source port:        $resv->{reservation_src_port}\n";
    }
    else { $msg .= "Source port:        DEFAULT\n"; }

    if ($resv->{reservation_dst_port}) {
        $msg .= "Destination port:   $resv->{reservation_dst_port}\n";
    }
    else { $msg .= "Destination port:   DEFAULT\n"; }

    if ($resv->{reservation_protocol} &&
        ($resv->{reservation_protocol} ne 'NULL')) {
        $msg .= "Protocol:           $resv->{reservation_protocol}\n";
    }
    else { $msg .= "Protocol:           DEFAULT\n"; }

    if ($resv->{reservation_dscp} && ($resv->{reservation_dscp} ne 'NU')) {
        $msg .= "DSCP:               $resv->{reservation_dscp}\n";
    }
    else { $msg .= "DSCP:               DEFAULT\n"; }

    if ($resv->{reservation_class}) {
        $msg .= "Class:              $resv->{reservation_class}\n";
    }
    else { $msg .= "Class:   DEFAULT\n"; }

    if ($resv->{ingress_router}) {
        $msg .= "Ingress loopback:   $resv->{ingress_router}\n";
    }
    else { $msg .= "Ingress loopback:   $resv->{ingress_ip}\n"; }

    if ($resv->{egress_router}) {
        $msg .= "Egress loopback:    $resv->{egress_router}\n";
    }
    else { $msg .= "Egress loopback:    $resv->{egress_ip}\n"; }
    
    $msg .= "$resv->{lsp_status}\n";
    return $msg;
} #____________________________________________________________________________

                   
######
1;
# vim: et ts=4 sw=4
