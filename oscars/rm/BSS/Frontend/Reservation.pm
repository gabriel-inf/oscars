# Reservation.pm:
# Last modified: June 8, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Reservation;

use strict;

use DateTime;
use Data::Dumper;

use BSS::Frontend::Database;

# until can get MySQL and views going
my @user_fields = ( 'reservation_id',
                    'user_dn',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_status',
                    'src_hostaddrs_id',
                    'dst_hostaddrs_id',
                    'reservation_tag');

my @detail_fields = ( 'reservation_id',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_created_time',
                    'reservation_bandwidth',
                    'reservation_burst_limit',
                    'reservation_status',
                    'src_hostaddrs_id',
                    'dst_hostaddrs_id',
                    'reservation_description',
                    'reservation_tag');


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
                       'configs' => $self->{configs})
                        or die "FATAL:  could not connect to database";
}
######


### Following methods called from ReservationHandler.

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
    my( %results );

    $results{error_msg} = $self->check_connection();
    if ($results{error_msg}) { return( 1, %results); }

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
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query( $query,
            $inref->{reservation_start_time}, $inref->{reservation_end_time});
    if ( $results{error_msg} ) { return( 1, %results ); }
    $arrayref = $sth->fetchall_arrayref({});

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.
    ($over_limit, $results{error_msg}) = $self->check_oversubscribe($arrayref, $inref);
    if ( $over_limit || $results{error_msg} ) {
        $sth->finish();
        return( 1, %results );
    }
    else {
        $sth->finish();

        if (($inref->{ingress_interface_id} == 0) ||
            ($inref->{egress_interface_id} == 0))
        {
            $results{error_msg} = "Invalid router id(s): 0.  Unable to " .
                                    "do insert.";
            return( 1, %results );
        }

        # get ipaddr id from host's and destination's ip addresses
        $inref->{src_hostaddrs_id} = $self->{dbconn}->hostaddrs_ip_to_id(
                                                  $inref->{src_hostaddrs_ip}); 
        $inref->{dst_hostaddrs_id} = $self->{dbconn}->hostaddrs_ip_to_id(
                                                  $inref->{dst_hostaddrs_ip}); 
        $inref->{reservation_created_time} = time();

        $query = "SHOW COLUMNS from reservations";
        ($sth, $results{error_msg}) = $self->{dbconn}->do_query( $query );
        if ( $results{error_msg} ) { return( 1, %results ); }
        $arrayref = $sth->fetchall_arrayref({});
        my @insertions;
        for $_ ( @$arrayref ) {
           if ($inref->{$_->{Field}}) {
               $results{$_} = $inref->{$_->{Field}};
               push(@insertions, $inref->{$_->{Field}}); 
           }
           else{ push(@insertions, 'NULL'); }
        }
        $sth->finish();

        # insert all fields for reservation into database
        $query = "INSERT INTO reservations VALUES (
                 " . join( ', ', ('?') x @insertions ) . " )";
        ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query,
                                                                 @insertions);
        if ( $results{error_msg} ) { return( 1, %results ); }

        $results{reservation_id} = $self->{dbconn}->{dbh}->{mysql_insertid};
    }
    $sth->finish();

    $results{reservation_tag} = $inref->{reservation_tag} . $results{reservation_id};
    $query = "UPDATE reservations SET reservation_tag = ?
              WHERE reservation_id = ?";
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query,
                                  $results{reservation_tag}, $results{reservation_id});
    if ( $results{error_msg} ) { return( 1, %results ); }

    $results{status_msg} = "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results{reservation_id}.";
    return( 0, %results );
}
######

###############################################################################
# delete_reservation:  Cancels the reservation by setting the reservation
# status to pending cancellation.
#
sub delete_reservation {
    my( $self, $inref ) = @_;

    return( $self->update_reservation( $inref,
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
    my( %mapping, $rref, $arrayref, $r, %results );

    $results{error_msg} = $self->check_connection();
    if ($results{error_msg}) { return( 1, %results); }

    # If administrator is making request, show all reservations.  Otherwise,
    # show only the user's reservations.  If id is given, show only the results
    # for that reservation.  Sort by start time in ascending order.
    if ($inref->{reservation_id}) {
        $query = "SELECT " . join(', ', @detail_fields);
        $query .= " FROM reservations";
        $query .= " WHERE reservation_id = $inref->{reservation_id}";
    }
    elsif ($inref->{user_dn}) {
        $query = "SELECT " . join(', ', @user_fields);
        $query .= " FROM reservations";
        $query .= " WHERE user_dn = '$inref->{user_dn}'";
    }
    $query .= " ORDER BY reservation_start_time";
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query);
    if ( $results{error_msg} ) { return( 1, %results ); }

    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $query = "SELECT hostaddrs_id, hostaddrs_ip FROM hostaddrs";
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query);
    if ( $results{error_msg} ) { return( 1, %results ); }

    $arrayref = $sth->fetchall_arrayref();
    for $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    my $k;
    for $r (@$rref) {
        $r->{src_hostaddrs_id} = $mapping{$r->{src_hostaddrs_id}};
        $r->{dst_hostaddrs_id} = $mapping{$r->{dst_hostaddrs_id}};
    }
    $results{rows} = $rref;

    $sth->finish();
    $results{status_msg} = 'Successfully read reservations';
    return( 0, %results );
}
######

### Following methods called from SchedulerThread.

###############################################################################
sub find_pending_reservations  { 
    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg );
    my ( %results );

    $results{error_msg} = $self->check_connection();
    if ($results{error_msg}) { return( 1, %results); }

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query, $status,
                                                            $stime);
    if ( $results{error_msg} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});
    # close it up
    $sth->finish();

    return( "", $data );
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg);
    my ( %results );

    $results{error_msg} = $self->check_connection();
    if ($results{error_msg}) { return( 1, %results); }

    #print "expired: Looking at time == " . $stime . "\n";

    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query, $status,
                                        $stime,
                                        $self->{configs}->{PENDING_CANCEL});
    if ( $results{error_msg} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish();

    # return the answer
    return( "", $data );
}
######


###############################################################################
# update_reservation: Updates reservation status.  Used to mark as active,
# finished, or cancelled.
#
sub update_reservation {
    my ( $self, $inref, $status ) = @_;
    my ( $rref, $sth, $query, %results );

    $results{error_msg} = $self->check_connection();
    if ($results{error_msg}) { return( 1, %results); }

    $query = qq{ SELECT reservation_status from reservations
                 WHERE reservation_id = ?};
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query,
                                                    $inref->{reservation_id});
    if ( $results{error_msg} ) { return( 1, %results ); }
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    # If the previous state was pending_cancel, mark it now as cancelled.
    # If the previous state was pending, and it is to be deleted, mark it
    # as cancelled instead of pending_cancel.  The latter is used by 
    # find_expired_reservations as one of the conditions to attempt to
    # tear down a circuit.
    my $prev_status = @{$rref}[0]->{reservation_status};
    if ( ($prev_status eq $self->{configs}->{PENDING_CANCEL}) ||
         ( ($prev_status eq $self->{configs}->{PENDING}) &&
            ($status eq $self->{configs}->{PENDING_CANCEL}))) { 
        $status = $self->{configs}->{CANCELLED};
    }
    $query = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    ($sth, $results{error_msg}) = $self->{dbconn}->do_query($query, $status,
                                                    $inref->{reservation_id});
    if ( $results{error_msg} ) { return( 1, %results ); }
    $sth->finish();
    $results{status_msg} = "Successfully updated reservation.";
    return( 0, %results );
}
######


###############################################################################
# to_bytes:  convert a string in the form of '100K' to 100000.
#
sub to_bytes {
    my ($self, $bytes) = @_;
    my ($mult);

    if ( $bytes =~ /(\d+)(K)/i ) {
        $mult = 1000;
    }
    elsif ($bytes =~ /(\d+)(M)/i )  {
        $mult = 1000000;
    }
    elsif ( $bytes =~ /(\d+)(G)/i )  { 
        $mult = 1000000000;
    } else {
        $mult = 1;
    }   
    return ($bytes * $mult)
}
######

###############################################################################
# get_interface_fields:  get the bandwidth of a router interface.
#
# IN: router interface idx
# OUT: interface row
#
sub get_interface_fields {
    my( $self, $iface_id) = @_;
    my( $results, $error_msg, $query, $sth);

    $query = "SELECT * FROM interfaces WHERE interface_id = ?";
    ($sth, $error_msg) = $self->{dbconn}->do_query($query, $iface_id);
    if ($error_msg) { return ( undef, $error_msg ); }

    $results = $sth->fetchrow_hashref();
    $sth->finish();

    return ( $results, '');
}
######

###############################################################################
# check_overscribe:  gets the list of active reservations at the same time as
#   this (proposed) reservation.  Also queries the db for the max speed of the
#   router interfaces to see if we have exceeded it.
#
# In: list of reservations, new reservation
# OUT: valid (0 or 1), and error message
#
sub check_oversubscribe {
    my ( $self, $reservations, $inref) = @_;
    my ( %iface_idxs, $row, $reservation_path, $link, $res, $idx );
    my ( $router_name, $error_msg );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my ( $max_reservation_utilization) = 0.50; 

    # assign the new path bandwidths 
    for $link (@{$inref->{reservation_path}}) {
        $iface_idxs{$link} = $self->to_bytes($inref->{reservation_bandwidth});
    }

    # loop through all active reservations
    for $res (@$reservations) {
        # get bandwidth allocated to each idx on that path
        for $reservation_path ( $res->{reservation_path} ) {
            for $link ( split(' ', $reservation_path) ) {
                $iface_idxs{$link} += $self->to_bytes($res->{reservation_bandwidth});
            }
        }
    }

    # now for each of those interface idx
    for $idx (keys %iface_idxs) {
        # get max bandwith speed for an idx
        ($row, $error_msg) = $self->get_interface_fields($idx);
        if ($error_msg) { return (1, $error_msg); }
        if (!$row ) { next; }

        if ( $row->{interface_valid} == 'False' ) {
            return ( 1, "interface $idx not valid");
        }
 
        if ($iface_idxs{$idx} >
            ($row->{interface_speed} * $max_reservation_utilization)) {
            my $max_utilization = $row->{interface_speed} * $max_reservation_utilization/1000000.0;
            if ($inref->{form_type} eq 'admin') {
                ($router_name, $error_msg) = $self->{dbconn}->xface_id_to_loopback($idx, 'name');
                if ($error_msg) { return (1, $error_msg); }
                $error_msg = "$router_name oversubscribed: ";
            }
            else {
                $error_msg = "Route oversubscribed: ";
            }
            return ( 1, $error_msg . $iface_idxs{$idx}/1000000 . " Mbps > " . "$max_utilization Mbps" );
        }
    }
    # Replace array @$inref->{reservation_path} with string separated by
    # spaces
    $inref->{reservation_path} = join(' ', @{$inref->{reservation_path}});
    return (0, "");
}
######


#################
# Private methods
#################

###############################################################################
sub check_connection {
    my ( $self ) = @_;
    my ( %attr ) = (
        PrintError => 0,
        RaiseError => 0,
    );
    $self->{dbconn}->{dbh} = DBI->connect(
             $self->{configs}->{use_BSS_database}, 
             $self->{configs}->{BSS_login_name},
             $self->{'configs'}->{BSS_login_passwd},
             \%attr);
    if (!($self->{dbconn}->{dbh})) {
        return("Unable to make database connection");
    }
    return "";
}
######

1;
# vim: et ts=4 sw=4
