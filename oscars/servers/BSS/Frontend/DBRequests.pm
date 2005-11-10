# DBRequests.pm:  database request methods for front end
#               inherits from Common::Database
# Last modified: November 8, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# Jason Lee (jrlee@lbl.gov)

package BSS::Frontend::DBRequests;

use strict; 

use Socket;

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Database;
use Common::Exception;

our @ISA = qw(Common::Database);

##############################################################################
sub new {
    my $invocant = shift;
    my $_class = ref($invocant) || $invocant;
    my ($_self) = {@_};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

######

###############################################################################
# update_status: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_status {
    my ( $self, $inref, $status ) = @_;

    my $query = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    my $rows = $self->do_query($query, $inref->{reservation_id});

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = $rows->[0]->{reservation_status};
    if ( ($prev_status eq 'precancel') || ( ($prev_status eq 'pending') &&
            ($status eq 'precancel'))) { 
        $status = 'cancelled';
    }
    $query = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    my $unused = $self->do_query($query, $status, $inref->{reservation_id});
    return( $status );
}
######

##############################################################################
#
sub get_pss_configs {
    my( $self ) = @_;

        # use defaults for now
    my $query = "SELECT " .
             "pss_conf_access, pss_conf_login, pss_conf_passwd, " .
             "pss_conf_firewall_marker, " .
             "pss_conf_setup_file, pss_conf_teardown_file, " .
             "pss_conf_ext_if_filter, pss_conf_CoS, " .
             "pss_conf_burst_limit, " .
             "pss_conf_setup_priority, pss_conf_resv_priority, " .
             "pss_conf_allow_lsp "  .
             "FROM pss_confs where pss_conf_id = 1";
    my $configs = $self->do_query($query);
    return( $configs );
}
######

##############################################################################
#
sub get_host_info {
    my( $self, $resvrows ) = @_;
 
    my( $resv, $ipaddr, $hrows );

    my $query = "SELECT hostaddr_ip FROM hostaddrs WHERE hostaddr_id = ?";
    for $resv (@$resvrows) {
        my $hrows = $self->do_query($query, $resv->{src_hostaddr_id});
        $resv->{source_ip} = $hrows->[0]->{hostaddr_ip};
        $ipaddr = inet_aton($resv->{source_ip});
        $resv->{source_host} = gethostbyaddr($ipaddr, AF_INET);
        if (!$resv->{source_host}) {
            $resv->{source_host} = $resv->{source_ip};
        }

        $hrows = $self->do_query($query, $resv->{dst_hostaddr_id});
        $resv->{destination_ip} = $hrows->[0]->{hostaddr_ip};
        $ipaddr = inet_aton($resv->{destination_ip});
        $resv->{destination_host} = gethostbyaddr($ipaddr, AF_INET);
        if (!$resv->{destination_host}) {
            $resv->{destination_host} = $resv->{destination_ip};
        }
    }
    return;
}
######

###############################################################################
# setup_times:  
#
sub setup_times {
    my( $self, $inref);

    my( $duration_seconds, $infinite_time );

    # Expects strings in second since epoch; converts to date in UTC time
    my $query = "SELECT CONVERT_TZ(from_unixtime(?), ?, '+00:00')" .
                " AS start_time";
    my $rows = $self->do_query( $query, $inref->{reservation_start_time},
                                $inref->{reservation_time_zone});
    $inref->{reservation_start_time} = $rows->[0]->{start_time};
    if ($inref->{duration_hour} < (2**31 - 1)) {
        $duration_seconds = $inref->{duration_hour} * 3600;
        $query = "SELECT DATE_ADD(?, INTERVAL ? SECOND) AS end_time";
        $rows = $self->do_query( $query, $inref->{reservation_start_time},
                                $duration_seconds );
        $inref->{reservation_end_time} = $rows->[0]->{end_time};
    }
    else {
        $inref->{reservation_end_time} = $infinite_time;
    }
    $query = "SELECT CONVERT_TZ(now(), ?, '+00:00') AS time_zone";
    $rows = $self->do_query( $query, $inref->{reservation_time_zone} );
    $inref->{reservation_created_time} = $rows->[0]->{time_zone};
}
######

##############################################################################
#
sub convert_times {
    my( $self, $resvrows ) = @_;
 
    my( $resv, $rows );

    # convert to time zone reservation was created in
    my $query = "SELECT CONVERT_TZ(?, '+00:00', ?) AS new_time";
    for $resv (@$resvrows) {
        $rows = $self->do_query( $query, $resv->{reservation_start_time},
                                   $resv->{reservation_time_zone} );
        $resv->{reservation_start_time} = $rows->[0]->{new_time};
        $rows = $self->do_query( $query, $resv->{reservation_end_time},
                                $resv->{reservation_time_zone} );
        $resv->{reservation_end_time} = $rows->[0]->{new_time};
        $rows = $self->do_query( $query, $resv->{reservation_created_time},
                                $resv->{reservation_time_zone} );
        $resv->{reservation_created_time} = $rows->[0]->{new_time};
    }
    return;
}
######

##############################################################################
#
sub get_engr_fields {
    my( $self, $resvrows ) = @_;
 
    my( $resv, $rows, @path_routers );

    my $query = "SELECT router_name, router_loopback FROM routers" .
                " WHERE router_id =" .
                  " (SELECT router_id FROM interfaces" .
                  "  WHERE interface_id = ?)";

    for $resv (@$resvrows) {
        $rows = $self->do_query($query, $resv->{ingress_interface_id});
        $resv->{ingress_router} = $rows->[0]->{router_name}; 
        $resv->{ingress_ip} = $rows->[0]->{router_loopback}; 

        $rows = $self->do_query($query, $resv->{egress_interface_id});
        $resv->{egress_router} = $rows->[0]->{router_name}; 
        $resv->{egress_ip} = $rows->[0]->{router_loopback}; 
        @path_routers = split(' ', $resv->{reservation_path});
        $resv->{reservation_path} = ();
        for $_ (@path_routers) {
            $rows = $self->do_query($query, $_);
            push(@{$resv->{reservation_path}}, $rows->[0]->{router_name}); 
        }
    }
    return;
}
######

1;
# vim: et ts=4 sw=4
