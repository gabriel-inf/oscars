#==============================================================================
package OSCARS::BSS::ReservationCommon;

=head1 NAME

OSCARS::BSS::ReservationCommon - Common functionality for OSCARS reservation 
methods.

=head1 SYNOPSIS

  use OSCARS::BSS::ReservationCommon;

=head1 DESCRIPTION

Common functionality for SOAP methods dealing with reservation creation, 
cancellation, and viewing.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 28, 2006

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
# view_details:  get reservation details from the database, given its
#     reservation id.  If a user has the proper authorization, he can view any 
#     reservation's details.  Otherwise he can only view reservations that
#     he has made, with less of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub view_details {
    my( $self, $params ) = @_;

    my( $statement, $row );

    my $user_login = $self->{user}->{login};
    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM BSS.reservations';
        $statement .= ' WHERE reservation_id = ?';
        $row = $self->{user}->get_row($statement, $params->{reservation_id});
        $self->get_engr_fields($row); 
    }
    else {
        $statement = 'SELECT reservation_start_time, reservation_end_time, ' .
            'reservation_created_time, reservation_bandwidth, ' .
            'reservation_burst_limit, reservation_status, reservation_class, ' .
            'reservation_src_port, reservation_dst_port, reservation_dscp, ' .
            'reservation_protocol, reservation_tag, reservation_description, ' .
            'src_host_id, dst_host_id, reservation_time_zone ' .
            'FROM BSS.reservations WHERE user_login = ? AND reservation_id = ?';
        $row = $self->{user}->get_row($statement, $user_login,
                                      $params->{reservation_id});
    }
    if (!$row) { return $row; }
    
    $self->get_host_info($row);
    $self->check_nulls($row);
    return $row;
} #____________________________________________________________________________


###############################################################################
# update_reservation: change the status of the reservervation from pending to
#                     active
#
sub update_reservation {
    my ($self, $resv, $status, $logger) = @_;

    if ( !$resv->{lsp_status} ) {
        $resv->{lsp_status} = "Successful configuration";
        $status = $self->update_status($resv->{reservation_id}, $status);
    } else {
        $status = $self->update_status($resv->{reservation_id}, 'failed');
    }
    $logger->info('update_reservation',
        { 'status' => $status, 'reservation_id' => $resv->{reservation_id} });
} #____________________________________________________________________________


###############################################################################
# update_status: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_status {
    my ( $self, $reservation_id, $status ) = @_;

    my $statement = qq{ SELECT reservation_status from BSS.reservations
                 WHERE reservation_id = ?};
    my $row = $self->{user}->get_row($statement, $reservation_id);

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = $row->{reservation_status};
    if ( ($prev_status eq 'precancel') || ( ($prev_status eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $statement = qq{ UPDATE BSS.reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    my $unused = $self->{user}->do_query($statement, $status, $reservation_id);
    return $status;
} #____________________________________________________________________________


###############################################################################
#
sub get_pss_configs {
    my( $self ) = @_;

        # use defaults for now
    my $statement = 'SELECT * FROM BSS.pss_confs where pss_conf_id = 1';
    my $configs = $self->{user}->get_row($statement);
    return $configs;
} #____________________________________________________________________________


###############################################################################
#
sub get_host_info {
    my( $self, $resv ) = @_;
 
    my $statement = 'SELECT host_ip FROM BSS.hosts WHERE host_id = ?';
    my $hrow = $self->{user}->get_row($statement, $resv->{src_host_id});
    my $ip = $hrow->{host_ip};
    my $regexp = '(\d+\.\d+\.\d+\.\d+)(/\d+)+';
    if ($ip =~ $regexp) { $ip = $1; }
    my $ipaddr = inet_aton($ip);
    $resv->{source_host} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{source_host}) {
        $resv->{source_host} = $hrow->{host_ip};
    }
    $resv->{source_ip} = $hrow->{host_ip};

    $hrow = $self->{user}->get_row($statement, $resv->{dst_host_id});
    # TODO:  FIX, hrow might be empty
    $ip = $hrow->{host_ip};
    if ($ip =~ $regexp) { $ip = $1; }
    $ipaddr = inet_aton($ip);
    $resv->{destination_host} = gethostbyaddr($ipaddr, AF_INET);
    if (!$resv->{destination_host}) {
        $resv->{destination_host} = $hrow->{host_ip};
    }
    $resv->{destination_ip} = $hrow->{host_ip};
} #____________________________________________________________________________


###############################################################################
#
sub get_engr_fields {
    my( $self, $resv ) = @_;
 
    my $statement = 'SELECT router_name, router_loopback FROM BSS.routers' .
                ' WHERE router_id =' .
                  ' (SELECT router_id FROM BSS.interfaces' .
                  '  WHERE interface_id = ?)';

    # TODO:  FIX row might be empty
    my $row = $self->{user}->get_row($statement, $resv->{ingress_interface_id});
    $resv->{ingress_router} = $row->{router_name}; 
    $resv->{ingress_ip} = $row->{router_loopback}; 

    $row = $self->{user}->get_row($statement, $resv->{egress_interface_id});
    $resv->{egress_router} = $row->{router_name}; 
    $resv->{egress_ip} = $row->{router_loopback}; 
    my @path_routers = split(' ', $resv->{reservation_path});
    $resv->{reservation_path} = ();
    for $_ (@path_routers) {
        $row = $self->{user}->get_row($statement, $_);
        push(@{$resv->{reservation_path}}, $row->{router_name}); 
    }
} #____________________________________________________________________________


###############################################################################
# host_ip_to_id:  get the primary key in the hosts table, given an
#     IP address.  A row is created if that address is not present.
# In:  host_ip
# Out: host_id
#
sub host_ip_to_id {
    my( $self, $ipaddr ) = @_;

    # TODO:  fix schema, possible host_ip would not be unique
    my $statement = 'SELECT host_id FROM BSS.hosts WHERE host_ip = ?';
    my $row = $self->{user}->get_row($statement, $ipaddr);
    # if no matches, insert a row in hosts
    if ( !$row ) {
        $statement = "INSERT INTO BSS.hosts VALUES ( NULL, '$ipaddr'  )";
        my $unused = $self->{user}->do_query($statement);
        return $self->{user}->{dbh}->{mysql_insertid};
    }
    else { return $row->{host_id}; }
} #____________________________________________________________________________


###############################################################################
# check_nulls:  
#
sub check_nulls {
    my( $self, $resv ) = @_ ;

    # clean up NULL values
    if (!$resv->{reservation_protocol} ||
        ($resv->{reservation_protocol} eq 'NULL')) {
        $resv->{reservation_protocol} = 'DEFAULT';
    }
    if (!$resv->{reservation_dscp} ||
        ($resv->{reservation_dscp} eq 'NU')) {
        $resv->{reservation_dscp} = 'DEFAULT';
    }
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


######
1;
# vim: et ts=4 sw=4
