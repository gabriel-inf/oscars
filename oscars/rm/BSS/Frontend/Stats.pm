# Stats.pm:  Reservation statistics formatting
# Last modified: July 1, 2005
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Stats;

use strict;

use Data::Dumper;

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;
}
######

##############################################################################
# get_stats
#
sub get_stats {
    my( $self, $user_dn, $inref, $results) = @_;

    # only optional fields need to be checked for existence
    my $stats = 
        "Reservation entered by $user_dn with parameters:\n" .
        "Description:      $inref->{reservation_description}\n";
    if ($results->{reservation_id}) {
        $stats .= "Reservation id:   $results->{reservation_id}\n";
    }
    $stats .= "Start time:       " .
            $self->get_time_str($inref->{reservation_start_time}) . "\n" .
        "End time:         " .
            $self->get_time_str($inref->{reservation_end_time}) . "\n";
    if ($inref->{reservation_created_time}) {
        $stats .= "Created time:     " .
            $self->get_time_str($inref->{reservation_created_time}) . "\n";
    }
    $stats .= "Bandwidth:        $inref->{reservation_bandwidth}\n";
    if ($inref->{reservation_burst_limit}) {
        $stats .= "Burst limit:      $inref->{reservation_burst_limit}\n";
    }
    $stats .= "Source:           $inref->{src_address}\n" .
        "Destination:      $inref->{dst_address}\n";
    if ($inref->{reservation_src_port}) {
        $stats .= "Source port:      $inref->{reservation_src_port}\n";
    }
    else { $stats .= "Source port:      default\n"; }

    if ($inref->{reservation_dst_port}) {
        $stats .= "Destination port: $inref->{reservation_dst_port}\n";
    }
    else { $stats .= "Destination port: default\n"; }

    if ($inref->{reservation_protocol}) {
        $stats .= "Protocol:         $inref->{reservation_protocol}\n";
    }
    else { $stats .= "Protocol:         default\n"; }

    if ($inref->{reservation_dscp}) {
        $stats .= "DSCP:             $inref->{reservation_dscp}\n";
    }
    else { $stats .= "DSCP:             default\n"; }

    if ($inref->{reservation_class}) {
        $stats .= "Class:            $inref->{reservation_class}\n\n";
    }
    else { $stats .= "Class:            default\n\n"; }

    return $stats;
}
######
                   
##############################################################################
# get_lsp_stats
#
sub get_lsp_stats {
    my( $self, $lsp_info, $inref, $status) = @_;

    my $stats = 
        "LSP config by $inref->{user_dn} with parameters:\n" .
        "Config time:      " .
            $self->get_time_str(time()) . "\n" .
        "Description:      $inref->{reservation_description}\n" .
        "Reservation id:   $inref->{reservation_id}\n" .
        "Start time:       " .
            $self->get_time_str($inref->{reservation_start_time}) . "\n" .
        "End time:         " .
            $self->get_time_str($inref->{reservation_end_time}) . "\n" .
        "Created time:     " .
            $self->get_time_str($inref->{reservation_created_time}) . "\n" .
        "Bandwidth:        $lsp_info->{bandwidth}\n" .
        "Burst limit:      $lsp_info->{'policer_burst-size-limit'}\n" .
        "Source:           $lsp_info->{'source-address'}\n" .
        "Destination:      $lsp_info->{'destination-address'}\n";
    if ($lsp_info->{'source-port'}) {
        $stats .= "Source port:      $lsp_info->{'source-port'}\n";
    }
    else { $stats .= "Source port:      default\n"; }

    if ($lsp_info->{'destination-port'}) {
        $stats .= "Destination port: $lsp_info->{'destination-port'}\n";
    }
    else { $stats .= "Destination port: default\n"; }

    if ($lsp_info->{protocol}) {
        $stats .= "Protocol:         $lsp_info->{protocol}\n";
    }
    else { $stats .= "Protocol:         default\n"; }

    if ($lsp_info->{dscp}) {
        $stats .= "DSCP:             $lsp_info->{dscp}\n";
    }
    else { $stats .= "DSCP:             default\n"; }

    if ($lsp_info->{'lsp_class-of-service'}) {
        $stats .= "Class:            $lsp_info->{'lsp_class-of-service'}\n";
    }
    else { $stats .= "Class:            default\n"; }

    if ($lsp_info->{lsp_from}) {
        $stats .= "Ingress loopback: $lsp_info->{lsp_from}\n";
    }
    else { $stats .= "Ingress loopback: WARNING:  None specified\n"; }

    if ($lsp_info->{lsp_to}) {
        $stats .= "Egress loopback: $lsp_info->{lsp_to}\n";
    }
    else { $stats .= "Egress loopback:  WARNING:  None specified\n"; }
    
    if ($status) {
        $stats .= "Error:  $status\n";
    }

    return $stats;
}
######
                   
## here temporarily

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $epoch_seconds, $gentag ) = @_;

    my( $second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year );
    my( $is_DST, $time_str );
    ($second, $minute, $hour, $day, $month, $year, $weekday, $day_of_year,
              $is_DST) = localtime($epoch_seconds); 
    $year += 1900;
    $month += 1;
    if ($month < 10) {
        $month = "0" . $month;
    }
    if ($day < 10) {
        $day = "0" . $day;
    }
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    if ($gentag ne 'tag') {
        $time_str = "$month-$day-$year at $hour:$minute";
    }
    else {
        $time_str = $year . $month . $day;
    }
    return( $time_str );
}
######

1;
# vim: et ts=4 sw=4
