package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: May 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DateTime;

use BSS::Frontend::Database;

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

###############################################################################
sub initialize {
    my ($self) = @_;

    $self->{'dbconn'} = BSS::Frontend::Database->new(
                       'configs' => $self->{'configs'})
                        or die "FATAL:  could not connect to database";
}


### Following methods called from ReservationHandler.

###############################################################################
## insert_reservation:  Called from the scheduler to insert a row into the
## reservations table.  Error checking so far is assumed to have been done  by
## scheduler and CGI script.
##
## IN:  reference to hash.  Hash's keys are all the fields of the reservations
##      table except for the primary key.
## OUT: error status (0 success, 1 failure), and the results hash.
###############################################################################
sub insert_reservation
{
    my( $self, $inref ) = @_;
    my( $query, $sth, $arrayref );
    my( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

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
    $inref->{'reservation_created_time'} = '';

    # handled query with the comparison start & end datetime strings
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query(
        $query, $inref->{'reservation_start_time'},
        $inref->{'reservation_end_time'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }
    $arrayref = $sth->fetchall_arrayref({});

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.
    ($over_limit, $results{'error_msg'}) = $self->check_oversubscribe($arrayref, $inref);
    if ( $over_limit || $results{'error_msg'} ) {
        $sth->finish();
        return( 1, %results );
    }
    else {
        $sth->finish();

        if (($inref->{'ingress_interface_id'} == 0) ||
            ($inref->{'egress_interface_id'} == 0))
        {
            $results{'error_msg'} = "Invalid router id(s): 0.  Unable to " .
                                    "do insert.";
            return( 1, %results );
        }

        # get ipaddr id from host's and destination's ip addresses
        $inref->{'src_hostaddrs_id'} = $self->{'dbconn'}->hostaddrs_ip_to_id(
                                           $inref->{'src_hostaddrs_ip'}); 
        $inref->{'dst_hostaddrs_id'} = $self->{'dbconn'}->hostaddrs_ip_to_id(
                                           $inref->{'dst_hostaddrs_ip'}); 
        $inref->{'reservation_created_time'} = time();

        my @insertions;   # copy over input fields that will be set in table
        my @fields_to_insert = $self->{'dbconn'}->get_fields_to_insert();
        foreach $_ ( @fields_to_insert ) {
           $results{$_} = $inref->{$_};
           push(@insertions, $inref->{$_}); 
        }

        # insert all fields for reservation into database
        $query = "INSERT INTO reservations VALUES (
                 " . join( ', ', ('?') x @insertions ) . " )";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                                 @insertions);
        if ( $results{'error_msg'} ) { return( 1, %results ); }

        $results{'id'} = $self->{'dbconn'}->{'dbh'}->{'mysql_insertid'};
    }
    $sth->finish();

        # insert reservation_tag field
    my $time_tag = get_time_str($inref->{'reservation_start_time'});
    $results{'reservation_tag'} = $inref->{'user_dn'} . '.' . $time_tag .
                                  "-" . $results{'id'};
    $query = "UPDATE reservations SET reservation_tag = ?
              WHERE reservation_id = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                  $results{'reservation_tag'}, $results{'id'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $results{'status_msg'} = "Your reservation has been processed " .
        "successfully. Your reservation ID number is $results{'id'}.";
    return( 0, %results );
}


###############################################################################
## delete_reservation:  Cancels the reservation by setting the reservation
## status to cancelled.
###############################################################################
sub delete_reservation
{
    my( $self, $inref ) = @_;

    return( $self->update_reservation(
                $inref, $self->{'configs'}->{'CANCELLED'}) );
}


###############################################################################
##  get_reservations: gets the reservation list from the database
## In: reference to hash of parameters
## Out: success or failure, and status message
###############################################################################
sub get_reservations
{
    my( $self, $inref, $fields_to_read ) = @_;
    my( $sth, $query );
    my( %mapping, @field_names, $rref, $arrayref, $r, %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    # DB query: get the reservation list
    # TODO:  selectall

    foreach $_ ( @$fields_to_read ) {
        push (@field_names, $_);
    }

    $query = "SELECT ";
    foreach $_ ( @field_names ) {
        $query .= $_ . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    # If administrator is making request, show all reservations.
    # Sort by start time in ascending order.
    if ($inref->{'admin_required'}) {
        $query .= " FROM reservations ORDER BY reservation_start_time";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    }
    else {
        $query .= " FROM reservations WHERE user_dn = ?
                    ORDER BY reservation_start_time";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                         $inref->{'user_dn'});
    }

    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $query = "SELECT hostaddrs_id, hostaddrs_ip FROM hostaddrs";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $arrayref = $sth->fetchall_arrayref();
    foreach $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    foreach $r (@$rref) {
        $r->{'src_hostaddrs_id'} = $mapping{$r->{'src_hostaddrs_id'}};
        $r->{'dst_hostaddrs_id'} = $mapping{$r->{'dst_hostaddrs_id'}};
    }
    $results{'rows'} = $rref;

    $sth->finish();
    $results{'status_msg'} = 'Successfully read reservations';
    return( 0, %results );
}


###############################################################################
## get_reservation_detail:  gets the reservation details from the database
## In: reference to hash of parameters
## Out: success or failure, and status message
###############################################################################
sub get_reservation_detail
{
    my( $self, $inref, $fields_to_display ) = @_;
    my( $sth, $query );
    my( %mapping, $r, $arrayref, %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @$fields_to_display ) {
        $query .= $_ . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM reservations WHERE reservation_id = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                  $inref->{'reservation_id'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # populate %results with the data fetched from the database
    @results{@$fields_to_display} = ();
    $sth->bind_columns( map { \$results{$_} } @$fields_to_display );
    $sth->fetch();
    $sth->finish();

    $query = "SELECT hostaddrs_id, hostaddrs_ip FROM hostaddrs";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $arrayref = $sth->fetchall_arrayref();
    foreach $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    $results{'src_hostaddrs_ip'} = $mapping{$results{'src_hostaddrs_id'}};
    $results{'dst_hostaddrs_ip'} = $mapping{$results{'dst_hostaddrs_id'}};

    $sth->finish();
    $results{'status_msg'} = 'Successfully got reservation details.';
    return (0, %results);
}


### Following methods called from SchedulerThread.

###############################################################################
sub find_pending_reservations  {

    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg );
    my ( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                             $status, $stime);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});
    # close it up
    $sth->finish();

    return( "", $data );
}


###############################################################################
sub find_expired_reservations
{
    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg);
    my ( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    #print "expired: Looking at time == " . $stime . "\n";

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_end_time < ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                              $status, $stime);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish();

    # return the answer
    return( "", $data );
}


###############################################################################
## update_reservation: Updates reservation status.  Used to mark as active,
## finished, or cancelled.
###############################################################################
sub update_reservation {

    my ( $self, $inref, $status ) = @_;
    my ( $sth, $query, %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    if ($status eq $self->{'configs'}->{'CANCELLED'}) {
        # This ensures that if the reservation is active, the LSP will be torn
        # down the next time find_expired_reservations runs.
        $query = qq{ UPDATE reservations SET reservation_end_time = 0
                     WHERE reservation_id = ?};
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                                  $inref->{'reservation_id'});
        if ( $results{'error_msg'} ) { return( 1, %results ); }
    }

    $query = qq{ UPDATE reservations SET reservation_status = ?
                 WHERE reservation_id = ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query,
                                          $status, $inref->{'reservation_id'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # close it up
    $sth->finish();
    $results{'status_msg'} = "Successfully updated reservation.";
    return( 0, %results );
}


############################################################
# Convert a string in the form of '100K' to 100000
############################################################

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
############################################################
# Get the bandwithd of a router interface
# IN: router interface idx
# OUT: interface row
############################################################
sub get_interface_fields {

    my( $self, $iface_id) = @_;
    my( $results, $error_msg, $query, $sth);

    $query = "SELECT * FROM interfaces WHERE interface_id = ?";
    ($sth, $error_msg) = $self->{'dbconn'}->do_query($query, $iface_id);
    if ($error_msg) { return ( undef, $error_msg ); }

    $results = $sth->fetchrow_hashref();

    return ( $results, '');
}

############################################################
# needs the list of active reserverations 
# at the same time as this (proposed) reservation
# will also need to query the db for the max 
# value of the router interfaces to see if we
# have exceeded it.
# IN: list of reserverations, new reserveration
# OUT: valid (0 or 1), and error message
############################################################
sub check_oversubscribe {
    my ( $self, $reservations, $inref) = @_;
    my ( %iface_idxs, @path_array, $row, $reservation_path, $link, $res, $idx );
    my ( $router_name, $error_msg );

    # XXX: what is the MAX percent we can allocate? for now 50% ...
    my ( $max_reservation_utilization) = 0.50; 

    @path_array = split(/,/, $inref->{'reservation_path'});
    # assign the new path bandwidths 
    foreach $link (@path_array) {
        $iface_idxs{$link} = $self->to_bytes($inref->{'reservation_bandwidth'});
    }

    # loop through all active reservations
    foreach $res (@$reservations) {
        # get bandwidth allocated to each idx on that path
        foreach $reservation_path ( $res->{'reservation_path'} ) {
            foreach $link ( split(',', $reservation_path) ) {
                $iface_idxs{$link} += $self->to_bytes($res->{'reservation_bandwidth'});
            }
        }
    }

    # now for each of those interface idx
    foreach $idx (keys %iface_idxs) {
        # get max bandwith speed for an idx
        ($row, $error_msg) = $self->get_interface_fields($idx);
        if ($error_msg) { return (1, $error_msg); }
        if (!$row ) { next; }

        if ( $row->{'interface_valid'} == 'False' ) {
            return ( 1, "interface $idx not valid");
        }
 
        if ($iface_idxs{$idx} > ($row->{'interface_speed'} * $max_reservation_utilization)) {
            my $max_utilization = $row->{'interface_speed'} * $max_reservation_utilization/1000000.0;
            if ($inref->{'admin_required'}) {
                ($router_name, $error_msg) = $self->{'dbconn'}->xface_id_to_loopback($idx);
                if ($error_msg) { return (1, $error_msg); }
                $error_msg = "$router_name oversubscribed: ";
            }
            else {
                $error_msg = "Route oversubscribed: ";
            }
            return ( 1, $error_msg . $iface_idxs{$idx}/1000000 . " Mbps > " . "$max_utilization Mbps" );
        }
    }
    return (0, "");
}

# vim: et ts=4 sw=4
## private

###############################################################################
sub check_connection
{
    my ( $self ) = @_;
    my ( %attr ) = (
        PrintError => 0,
        RaiseError => 0,
    );
    $self->{'dbconn'}->{'dbh'} = DBI->connect(
             $self->{'configs'}->{'use_BSS_database'}, 
             $self->{'configs'}->{'BSS_login_name'},
             $self->{'configs'}->{'BSS_login_passwd'},
             \%attr);
    if (!$self->{'dbconn'}->{'dbh'}) { return(
                                       "Unable to make database connection"); }
    return "";
}

###############################################################################
sub get_time_str 
{
    my( $epoch_seconds ) = @_;

    my $dt = DateTime->from_epoch( epoch => $epoch_seconds );
    my $year = $dt->year();
    if ($year < 10) {
        $year = "0" . $year;
    }
    my $month = $dt->month();
    if ($month < 10) {
        $month = "0" . $month;
    }
    my $day = $dt->day();
    if ($day < 10) {
        $day = "0" . $day;
    }
    my $hour = $dt->hour();
    if ($hour < 10) {
        $hour = "0" . $hour;
    }
    my $minute = $dt->minute();
    if ($minute < 10) {
        $minute = "0" . $minute;
    }
    my $time_tag = $year . $month . $day;

    return( $time_tag );
}


1;
