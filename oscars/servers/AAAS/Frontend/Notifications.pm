###############################################################################
package AAAS::Frontend::Notifications;

# Reservation statistics formatting.
#
# Last modified:  November 28, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# create_reservation
#
sub create_reservation {
    my( $self, $user_dn, $resv) = @_;

    my $msg = "Reservation scheduled by $user_dn with parameters:\n";
    $msg .= $self->reservation_stats($resv);
    my $subject_line = "Reservation scheduled for $user_dn";
    return( $subject_line, $msg );
} #____________________________________________________________________________


###############################################################################
# cancel_reservation
#
sub cancel_reservation {
    my( $self, $user_dn, $resv) = @_;

    my $msg = "Reservation cancelled by $user_dn with parameters:\n";
    $msg .= $self->reservation_stats($resv);
    my $subject_line = "Reservation cancelled by $user_dn";
    return( $subject_line, $msg );
} #____________________________________________________________________________


###############################################################################
# reservation_stats
#
sub reservation_stats {
    my( $self, $resv) = @_;

    # TODO:  FIX! infinite_time
    my $infinite_time = 'foo';
    # only optional fields need to be checked for existence
    my $msg = "Description:        $resv->{reservation_description}\n";
    if ($resv->{reservation_id}) {
        $msg .= "Reservation id:     $resv->{reservation_id}\n";
    }
    $msg .= "Start time:         $resv->{reservation_start_time}\n";
    if ($resv->{reservation_end_time} ne $infinite_time) {
        $msg .= "End time:           $resv->{reservation_end_time}\n";
    }
    else {
        $msg .= "End time:           persistent circuit\n";
    }

    if ($resv->{reservation_created_time}) {
        $msg .= "Created time:       $resv->{reservation_created_time}\n";
    }
    $msg .= "(Times are in UTC $resv->{reservation_time_zone})\n";
    $msg .= "Bandwidth:          $resv->{reservation_bandwidth}\n";
    if ($resv->{reservation_burst_limit}) {
        $msg .= "Burst limit:         $resv->{reservation_burst_limit}\n";
    }
    $msg .= "Source:             $resv->{source_host}\n" .
        "Destination:        $resv->{destination_host}\n";
    if ($resv->{reservation_src_port}) {
        $msg .= "Source port:        $resv->{reservation_src_port}\n";
    }
    else { $msg .= "Source port:        DEFAULT\n"; }

    if ($resv->{reservation_dst_port}) {
        $msg .= "Destination port:   $resv->{reservation_dst_port}\n";
    }
    else { $msg .= "Destination port:   DEFAULT\n"; }

    $msg .= "Protocol:           $resv->{reservation_protocol}\n";
    $msg .= "DSCP:               $resv->{reservation_dscp}\n";

    if ($resv->{reservation_class}) {
        $msg .= "Class:              $resv->{reservation_class}\n\n";
    }
    else { $msg .= "Class:              DEFAULT\n\n"; }

    return( $msg );
} #____________________________________________________________________________


###############################################################################
# find_pending_reservations
#
sub find_pending_reservations {
    my( $self, $user_dn, $reservations) = @_;

    my $subject_line = "Circuit set up status for $user_dn";
    my $msg =
          "Circuit set up for $user_dn, for reservation(s) with parameters:\n";
    for my $resv ( @$reservations ) {
            # TODO:  if more than one reservation, fix duplicated effort
        $self->convert_times($resv);
        $msg .= $self->reservation_lsp_stats($user_dn, $resv);
    }
    return( $subject_line, $msg );
} #____________________________________________________________________________


###############################################################################
# find_expired_reservations
#
sub find_expired_reservations {
    my( $self, $user_dn, $reservations) = @_;

    my $subject_line = "Circuit tear down status for $user_dn";
    my $msg =
       "Circuit tear down for $user_dn, for reservation(s) with parameters:\n";
    for my $resv ( @$reservations ) {
        $self->convert_times($resv);
        $msg .= $self->reservation_lsp_stats( $resv );
    }
    return( $subject_line, $msg );
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
        "Source:             $resv->{source_host}\n" .
        "Destination:        $resv->{destination_host}\n";
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

                   
###############################################################################
#
sub convert_times {
    my( $self, $resv ) = @_;

    my $statement = "SELECT CONVERT_TZ(now(), '+00:00', ?) AS newtime";
    my $row = $self->{dbconn}->get_row( $statement,
                                        $resv->{reservation_time_zone});
    $resv->{lsp_config_time} = $row->{newtime};
    $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS newtime";
    $row = $self->{dbconn}->get_row( $statement, $resv->{reservation_start_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $row->{newtime};
    $row = $self->{dbconn}->get_row( $statement, $resv->{reservation_end_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $row->{newtime};
    $row = $self->{dbconn}->get_row( $statement,
                                     $resv->{reservation_created_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $row->{newtime};
} #____________________________________________________________________________ 


######
1;
# vim: et ts=4 sw=4
