# Reservation.pm:  Database handling for BSS/Scheduler/ReservationHandler.pm
# Last modified: October 18, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Reservation;

use strict;

use Error qw(:try);
use Data::Dumper;

use Common::Mail;
use Common::Exception;
use BSS::Frontend::Database;
use BSS::Frontend::Policy;
use BSS::Frontend::Stats;

# until can get MySQL and views going
my @user_fields = ( 'reservation_id',
                    'user_dn',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_tag');

my @detail_fields = ( 'reservation_id',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_created_time',
                    'reservation_time_zone',
                    'reservation_bandwidth',
                    'reservation_burst_limit',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_description',
                    'reservation_src_port',
                    'reservation_dst_port',
                    'reservation_dscp',
                    'reservation_protocol',
                    'reservation_tag');

my @detail_admin_fields = ( 'ingress_interface_id',
                    'egress_interface_id',
                    'reservation_path',
                    'reservation_class');


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

    $self->{policy} = BSS::Frontend::Policy->new(
                       'dbconn' => $self->{dbconn});
}
######


###############################################################################
sub logout {
    my( $self, $inref ) = @_;

    return( $self->{dbconn}->logout($inref->{user_dn}) );
}
######

###############################################################################
# insert_reservation:  Called from the scheduler to insert a row into the
#   reservations table.  Error checking so far is assumed to have been done by
#   scheduler and CGI script.
#
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: results hash.
#
sub insert_reservation {
    my( $self, $inref ) = @_;
    my( $query, $sth, $arrayref, @resv_array );
    my $results = {};
    my $user_dn = $inref->{user_dn};
    my( $duration_seconds );

    if (($inref->{ingress_interface_id} == 0) ||
        ($inref->{egress_interface_id} == 0))
    {
        throw Common::Exception("Invalid router id(s): 0.  Unable to " .
                                "do insert.");
    }
    $self->{dbconn}->login_user($user_dn);
    # convert requested bandwidth to bps
    $inref->{reservation_bandwidth} *= 1000000;
    my $stats = BSS::Frontend::Stats->new();
    # Expects strings in seoncds since epoch; converts to date in UTC time
    $query = "SELECT CONVERT_TZ(from_unixtime(?), ?, '+00:00')";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_start_time},
                                      $inref->{reservation_time_zone});
    $inref->{reservation_start_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    if ($inref->{duration_hour} < (2**31 - 1)) {
        $duration_seconds = $inref->{duration_hour} * 3600;
        $query = "SELECT DATE_ADD(?, INTERVAL ? SECOND)";
        $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                          $inref->{reservation_start_time},
                                          $duration_seconds );
        $inref->{reservation_end_time} = $sth->fetchrow_arrayref()->[0];
        $sth->finish();
    }
    else {
        $inref->{reservation_end_time} = $stats->get_infinite_time();
    }
    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    $query = "SELECT reservation_bandwidth, reservation_start_time,
              reservation_end_time, reservation_path FROM reservations
              WHERE reservation_end_time >= ? AND
                  reservation_start_time <= ? AND
                  (reservation_status = 'pending' OR
                   reservation_status = 'active')";

    # only holds a time if reservation successful
    $inref->{reservation_created_time} = '';

    # handled query with the comparison start & end datetime strings
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
           $inref->{reservation_start_time}, $inref->{reservation_end_time});
    $arrayref = $sth->fetchall_arrayref({});

    # If no segment is over the limit,  record the reservation to the database.
    # Otherwise, throw exception with the bandwidths and times involved.
    $self->{policy}->check_oversubscribe($arrayref, $inref);
    $sth->finish();

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $inref->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                $inref->{source_ip}); 
    $inref->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                                            $inref->{destination_ip}); 
    $query = "SELECT CONVERT_TZ(now(), ?, '+00:00')";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_time_zone} );
    $inref->{reservation_created_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();

    $self->{pss_configs} = $self->{dbconn}->get_pss_configs();
    $inref->{reservation_id} = 'NULL';
        # class of service
    $inref->{reservation_class} = $self->{pss_configs}->{pss_conf_CoS};
    $inref->{reservation_burst_limit} =
                                $self->{pss_configs}->{pss_conf_burst_limit};
    $inref->{reservation_status} = 'pending';
    $inref->{reservation_tag} = $user_dn . '.' .
        $stats->get_time_str($inref->{reservation_start_time}, 'tag') .  "-";

    $query = "SHOW COLUMNS from reservations";
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    $arrayref = $sth->fetchall_arrayref({});
    my @insertions;
    for $_ ( @$arrayref ) {
       if ($inref->{$_->{Field}}) {
           $results->{$_->{Field}} = $inref->{$_->{Field}};
           push(@insertions, $inref->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }
    $sth->finish();

    # insert all fields for reservation into database
    $query = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    $sth = $self->{dbconn}->do_query($user_dn, $query, @insertions);
    $results->{reservation_id} = $self->{dbconn}->get_reservation_id($user_dn);
    $sth->finish();

    $results->{reservation_tag} =
        $inref->{reservation_tag} . $results->{reservation_id};
    $query = "UPDATE reservations SET reservation_tag = ?
              WHERE reservation_id = ?";
    $sth = $self->{dbconn}->do_query($user_dn, $query,
        $results->{reservation_tag}, $results->{reservation_id});
    $sth->finish();

    @resv_array = ($results);
    # convert times back to user's time zone for mail message
    $self->convert_times($user_dn, \@resv_array);
    $sth->finish();
    $results->{rows} = \@resv_array;

    my $mailer = Common::Mail->new();
    my $mail_msg = $stats->get_stats($user_dn, $results) ;
    $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "Reservation made by $user_dn", $mail_msg);
    $mailer->send_mail($mailer->get_webmaster(), $user_dn,
                       "Your reservation has been accepted", $mail_msg);
    return( $results );
}
######

###############################################################################
# delete_reservation:  Cancels the reservation by setting the reservation
# status to pending cancellation.
#
sub delete_reservation {
    my( $self, $inref ) = @_;

    my $status =  $self->{dbconn}->update_reservation( $inref->{user_dn}, $inref,
                                     'precancel' );
    return($self->get_reservations($inref));
}
######

###############################################################################
# get_reservations: gets the reservation list from the database
#
# In: reference to hash of parameters, id if only want one reservation
# Out: success or failure, and status message
#
sub get_reservations {
    my( $self, $inref ) = @_;

    my( $sth, $query );
    my( $rref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $self->{dbconn}->login_user($user_dn);
    # If administrator is making request, show all reservations.  Otherwise,
    # show only the user's reservations.  If id is given, show only the results
    # for that reservation.  Sort by start time in ascending order.
    if ($inref->{reservation_id}) {
        if ( !($self->{policy}->authorized($inref->{user_level}, "engr")) ) {
            $query = "SELECT " . join(', ', @detail_fields);
        }
        else {
            $query = "SELECT " .
                     join(', ', (@detail_fields, @detail_admin_fields));
        }
        $query .= " FROM reservations" .
                  " WHERE reservation_id = $inref->{reservation_id}";
    }
    elsif ($user_dn) {
        if ( $self->{policy}->authorized($inref->{user_level}, "engr") ) {
            $query = "SELECT * FROM reservations";
        }
        else {
            $query = "SELECT " . join(', ', @user_fields);
            $query .= " FROM reservations" .
                      " WHERE user_dn = '$user_dn'";
        }
    }
    $query .= " ORDER BY reservation_start_time";
    $sth = $self->{dbconn}->do_query($user_dn, $query);
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $self->get_hosts($user_dn, $rref);
    $self->convert_times($user_dn, $rref);
    if ($self->{policy}->authorized($inref->{user_level}, "engr") &&
        $inref->{reservation_id}) {
        # in this case, only one row
        $self->get_engr_fields($user_dn, @$rref[0]); 
    }
    $results->{rows} = $rref;
    return( $results );
}
######

#################
# Private methods
#################

##############################################################################
#
sub get_hosts {
    my( $self, $user_dn, $rref ) = @_;
 
    my( $resv, %mapping );

    my $query = "SELECT hostaddr_id, hostaddr_ip FROM hostaddrs";
    my $sth = $self->{dbconn}->do_query($user_dn, $query);
    my $arrayref = $sth->fetchall_arrayref();
    for $resv (@$arrayref) { $mapping{$$resv[0]} = $$resv[1]; }
    $sth->finish();

    for $resv (@$rref) {
        $resv->{source_host} = $mapping{$resv->{src_hostaddr_id}};
        $resv->{destination_host} = $mapping{$resv->{dst_hostaddr_id}};
    }
    return;
}
######

##############################################################################
#
sub convert_times {
    my( $self, $user_dn, $rref ) = @_;
 
    my( $resv, $sth, $query );

    # convert to time zone reservation was created in
    $query = "SELECT CONVERT_TZ(?, '+00:00', ?)";
    for $resv (@$rref) {
        $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_start_time},
                                      $resv->{reservation_time_zone} );
        $resv->{reservation_start_time} = $sth->fetchrow_arrayref()->[0];
        $sth->finish();
        $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_end_time},
                                      $resv->{reservation_time_zone} );
        $resv->{reservation_end_time} = $sth->fetchrow_arrayref()->[0];
        $sth->finish();
        $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_created_time},
                                      $resv->{reservation_time_zone} );
        $resv->{reservation_created_time} = $sth->fetchrow_arrayref()->[0];
        $sth->finish();
    }
    return;
}
######

##############################################################################
#
sub get_engr_fields {
    my( $self, $user_dn, $resv ) = @_;
 
    my( $hashref, @path_routers, $sth, $query );

    $query = "SELECT router_name, router_loopback FROM routers" .
             " WHERE router_id =" .
                  " (SELECT router_id FROM interfaces" .
                  "  WHERE interface_id = ?)";

    $sth = $self->{dbconn}->do_query($user_dn, $query,
                                               $resv->{ingress_interface_id});
    $hashref = $sth->fetchrow_hashref();
    $resv->{ingress_router} = $hashref->{router_name}; 
    $sth->finish();

    $sth = $self->{dbconn}->do_query($user_dn, $query,
                                     $resv->{egress_interface_id});
    $hashref = $sth->fetchrow_hashref();
    $resv->{egress_router} = $hashref->{router_name}; 
    @path_routers = split(' ', $resv->{reservation_path});
    $resv->{reservation_path} = ();
    $sth->finish();
    for $_ (@path_routers) {
        $sth = $self->{dbconn}->do_query($user_dn, $query, $_);
        $hashref = $sth->fetchrow_hashref();
        push(@{$resv->{reservation_path}}, $hashref->{router_name}); 
        $sth->finish();
    }
    return;
}
######

1;
# vim: et ts=4 sw=4
