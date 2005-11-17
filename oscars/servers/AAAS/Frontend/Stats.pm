package BSS::Frontend::Stats;

# Reservation statistics formatting.
#
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# TODO:  fix, keep here, but send formatted message back to AAAS
#        need to instantiate stats class somewhere


use strict;

use Data::Dumper;

###############################################################################
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;
}
######

##############################################################################
# get_stats
#
sub get_stats {
    my( $self, $resv, $infinite_time) = @_;

    # only optional fields need to be checked for existence
    my $stats = 
        "Reservation entered by $resv->{user_dn} with parameters:\n" .
        "Description:        $resv->{reservation_description}\n";
    if ($resv->{reservation_id}) {
        $stats .= "Reservation id:     $resv->{reservation_id}\n";
    }
    $stats .= "Start time:         $resv->{reservation_start_time}\n";
    if ($resv->{reservation_end_time} ne $infinite_time) {
        $stats .= "End time:           $resv->{reservation_end_time}\n";
    }
    else {
        $stats .= "End time:           persistent circuit\n";
    }

    if ($resv->{reservation_created_time}) {
        $stats .= "Created time:       $resv->{reservation_created_time}\n";
    }
    $stats .= "(Times are in UTC $resv->{reservation_time_zone})\n";
    $stats .= "Bandwidth:          $resv->{reservation_bandwidth}\n";
    if ($resv->{reservation_burst_limit}) {
        $stats .= "Burst limit:         $resv->{reservation_burst_limit}\n";
    }
    $stats .= "Source:             $resv->{source_host}\n" .
        "Destination:        $resv->{destination_host}\n";
    if ($resv->{reservation_src_port}) {
        $stats .= "Source port:        $resv->{reservation_src_port}\n";
    }
    else { $stats .= "Source port:        DEFAULT\n"; }

    if ($resv->{reservation_dst_port}) {
        $stats .= "Destination port:   $resv->{reservation_dst_port}\n";
    }
    else { $stats .= "Destination port:   DEFAULT\n"; }

    $stats .= "Protocol:           $resv->{reservation_protocol}\n";
    $stats .= "DSCP:               $resv->{reservation_dscp}\n";

    if ($resv->{reservation_class}) {
        $stats .= "Class:              $resv->{reservation_class}\n\n";
    }
    else { $stats .= "Class:              DEFAULT\n\n"; }

    return $stats;
}
######
                   
##############################################################################
# get_lsp_stats
#
sub get_lsp_stats {
    my( $self, $resv, $status, $config_time) = @_;

    my $stats = 
        "LSP config by $resv->{user_dn} with parameters:\n" .
        "Config time:        $config_time\n" .
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
        $stats .= "Source port:        $resv->{reservation_src_port}\n";
    }
    else { $stats .= "Source port:        DEFAULT\n"; }

    if ($resv->{reservation_dst_port}) {
        $stats .= "Destination port:   $resv->{reservation_dst_port}\n";
    }
    else { $stats .= "Destination port:   DEFAULT\n"; }

    if ($resv->{reservation_protocol} &&
        ($resv->{reservation_protocol} ne 'NULL')) {
        $stats .= "Protocol:           $resv->{reservation_protocol}\n";
    }
    else { $stats .= "Protocol:           DEFAULT\n"; }

    if ($resv->{reservation_dscp} && ($resv->{reservation_dscp} ne 'NU')) {
        $stats .= "DSCP:               $resv->{reservation_dscp}\n";
    }
    else { $stats .= "DSCP:               DEFAULT\n"; }

    if ($resv->{reservation_class}) {
        $stats .= "Class:              $resv->{reservation_class}\n";
    }
    else { $stats .= "Class:   DEFAULT\n"; }

    if ($resv->{ingress_router}) {
        $stats .= "Ingress loopback:   $resv->{ingress_router}\n";
    }
    else { $stats .= "Ingress loopback:   $resv->{ingress_ip}\n"; }

    if ($resv->{egress_router}) {
        $stats .= "Egress loopback:    $resv->{egress_router}\n";
    }
    else { $stats .= "Egress loopback:    $resv->{egress_ip}\n"; }
    
    if ($status) {
        $stats .= "Error:              $status\n";
    }
    else { $stats .= "\n\nLSP configuration successful.\n"; }

    return $stats;
}
######
                   
1;
# vim: et ts=4 sw=4
