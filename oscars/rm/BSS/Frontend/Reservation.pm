# Reservation.pm:  Database handling for BSS/Scheduler/ReservationHandler.pm
# Last modified: June 30, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Reservation;

use strict;

use Data::Dumper;

use Common::Mail;
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

    $self->{dbconn} = BSS::Frontend::Database->new(
                       'database' => $self->{configs}->{use_BSS_database},
                       'login' => $self->{configs}->{BSS_login_name},
                       'password' => $self->{configs}->{BSS_login_passwd},
                       'configs' => $self->{configs})
                        or die "FATAL:  could not connect to database";
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
# Out: error status (0 success, 1 failure), and the results hash.
#
sub insert_reservation {
    my( $self, $inref ) = @_;
    my( $query, $sth, $arrayref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    # whether any time segment is over the bandwidth limit
    my $over_limit = 0;

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
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query( $user_dn, $query,
            $inref->{reservation_start_time}, $inref->{reservation_end_time});
    if ( $results->{error_msg} ) { return( 1, $results ); }
    $arrayref = $sth->fetchall_arrayref({});

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.
    ($over_limit, $results->{error_msg}) = $self->{policy}->check_oversubscribe($arrayref, $inref);
    if ( $over_limit || $results->{error_msg} ) {
        $sth->finish();
        return( 1, $results );
    }
    else {
        $sth->finish();

        if (($inref->{ingress_interface_id} == 0) ||
            ($inref->{egress_interface_id} == 0))
        {
            $results->{error_msg} = "Invalid router id(s): 0.  Unable to " .
                                    "do insert.";
            return( 1, $results );
        }

        # get ipaddr id from host's and destination's ip addresses
        $inref->{src_hostaddr_id} = $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                                                  $inref->{src_address}); 
        $inref->{dst_hostaddr_id} = $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                                                  $inref->{dst_address}); 
        $inref->{reservation_created_time} = time();

        $query = "SHOW COLUMNS from reservations";
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query( $user_dn, $query );
        if ( $results->{error_msg} ) { return( 1, $results ); }
        $arrayref = $sth->fetchall_arrayref({});
        my @insertions;
        for $_ ( @$arrayref ) {
           if ($inref->{$_->{Field}}) {
               $results->{$_} = $inref->{$_->{Field}};
               push(@insertions, $inref->{$_->{Field}}); 
           }
           else{ push(@insertions, 'NULL'); }
        }
        $sth->finish();

        # insert all fields for reservation into database
        $query = "INSERT INTO reservations VALUES (
                 " . join( ', ', ('?') x @insertions ) . " )";
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                                 @insertions);
        if ( $results->{error_msg} ) { return( 1, $results ); }

        $results->{reservation_id} = $self->{dbconn}->{handles}->{$user_dn}->{mysql_insertid};
    }
    $sth->finish();

    $results->{reservation_tag} = $inref->{reservation_tag} . $results->{reservation_id};
    $query = "UPDATE reservations SET reservation_tag = ?
              WHERE reservation_id = ?";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                  $results->{reservation_tag}, $results->{reservation_id});
    if ( $results->{error_msg} ) { return( 1, $results ); }

    my $mailer = Common::Mail->new();
    my $stats = BSS::Frontend::Stats->new();
    my $mail_msg = $stats->get_stats($user_dn, $inref, $results) ;
    $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "Reservation made by $user_dn", $mail_msg);
    
    $results->{status_msg} = "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results->{reservation_id}.";
    return( 0, $results );
}
######

###############################################################################
# delete_reservation:  Cancels the reservation by setting the reservation
# status to pending cancellation.
#
sub delete_reservation {
    my( $self, $inref ) = @_;

    return( $self->{dbconn}->update_reservation( $inref->{user_dn}, $inref,
                                     $self->{configs}->{PENDING_CANCEL}) );
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
    my( %mapping, $rref, $arrayref, $r );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    # If administrator is making request, show all reservations.  Otherwise,
    # show only the user's reservations.  If id is given, show only the results
    # for that reservation.  Sort by start time in ascending order.
    if ($inref->{reservation_id}) {
        if ($inref->{user_level} ne 'engr') {
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
        if ($inref->{user_level} eq 'engr') {
            $query = "SELECT * FROM reservations";
        }
        else {
            $query = "SELECT " . join(', ', @user_fields);
            $query .= " FROM reservations" .
                      " WHERE user_dn = '$user_dn'";
        }
    }
    $query .= " ORDER BY reservation_start_time";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $query = "SELECT hostaddr_id, hostaddr_ip FROM hostaddrs";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    $arrayref = $sth->fetchall_arrayref();
    for $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    my $k;
    for $r (@$rref) {
        $r->{src_address} = $mapping{$r->{src_hostaddr_id}};
        $r->{dst_address} = $mapping{$r->{dst_hostaddr_id}};
    }

    if (($inref->{user_level} eq 'engr') &&
        $inref->{reservation_id}) {
        my $hashref;
        my @path_routers;

        $r = @$rref[0];  # in this case, only one row
        $query = "SELECT router_name, router_loopback FROM routers";
        $query .= " WHERE router_id =" .
                  " (SELECT router_id FROM interfaces" .
                  "  WHERE interface_id = ?)";

        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                               $query,
                                               $r->{ingress_interface_id});
        if ( $results->{error_msg} ) { return( 1, $results ); }
        $hashref = $sth->fetchrow_hashref();
        $r->{ingress_router_name} = $hashref->{router_name}; 
        $r->{ingress_loopback} = $hashref->{router_loopback};
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                $query,
                                                $r->{egress_interface_id});
        if ( $results->{error_msg} ) { return( 1, $results ); }
        $hashref = $sth->fetchrow_hashref();
        $r->{egress_router_name} = $hashref->{router_name}; 
        $r->{egress_loopback} = $hashref->{router_loopback};
        @path_routers = split(' ', $r->{reservation_path});
        $r->{reservation_path} = ();
        for $_ (@path_routers) {
            ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                $query, $_);
            if ( $results->{error_msg} ) { return( 1, $results ); }
            $hashref = $sth->fetchrow_hashref();
            push(@{$r->{reservation_path}}, $hashref->{router_name}); 
        }
    }

    $results->{rows} = $rref;
    $sth->finish();
    $results->{status_msg} = 'Successfully read reservations';
    return( 0, $results );
}
######

1;
# vim: et ts=4 sw=4
