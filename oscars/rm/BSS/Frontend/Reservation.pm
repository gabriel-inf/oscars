package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: May 9, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use BSS::Frontend::Database;

use Data::Dumper;

######################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

######################################################################
sub initialize {
    my ($self) = @_;

    $self->{'dbconn'} = BSS::Frontend::Database->new('configs' => $self->{'configs'}) or die "FATAL:  could not connect to database";
}


### Following methods called from ReservationHandler.

################################
### insert_reservation
###
### Called from the scheduler to insert a row into the reservations table.
### Error checking has already been done by scheduler and CGI script.
###
### IN:  reference to hash.  Hash's keys are all the fields of the reservations
###      table except for the primary key.
### OUT: error status (0 success, 1 failure), and the results hash.
################################

sub insert_reservation
{
    my( $self, $inref ) = @_;
    my( $query, $sth );
    my( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    my $over_limit = 0; # whether any time segment is over the bandwidth limit

    ###
    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
    $query = "SELECT reservation_bandwidth, reservation_start_time, reservation_end_time FROM reservations WHERE reservation_end_time >= ? AND reservation_start_time <= ?";

    $inref->{'reservation_created_time'} = '';  # only holds a time if reservation successful

    # handled query with the comparison start & end datetime strings

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'reservation_start_time'}, $inref->{'reservation_end_time'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # TODO:  find segments of overlap, determine if bandwidth in any is
    #        over the limit; return time segments if error

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.

    #$results{'id'} unless ( $over_limit );
    if ( $over_limit ) {
        $sth->finish();
        # TODO:  list of times
        results{'error_msg'} = '[ERROR] The available bandwidth limit on the network has been reached between '. 'Please modify your reservation request and try again.';
        return( 1, %results );
    }
    else {
        $sth->finish();

        if (($inref->{'ingress_interface_id'} == 0) || ($inref->{'egress_interface_id'} == 0))
        {
            $results{'error_msg'} = 'Invalid router id(s): 0.  Unable to do insert.';
            return( 1, %results );
        }

        # get ipaddr id from host's and destination's ip addresses
        $inref->{'src_hostaddrs_id'} = $self->{'dbconn'}->hostaddrs_ip_to_id($inref->{'src_hostaddrs_ip'}); 
        $inref->{'dst_hostaddrs_id'} = $self->{'dbconn'}->hostaddrs_ip_to_id($inref->{'dst_hostaddrs_ip'}); 
        $inref->{'reservation_created_time'} = time();

        my @insertions;   # copy over input fields that will be set in table
        my @fields_to_insert = $self->{'dbconn'}->get_fields_to_insert();
        foreach $_ ( @fields_to_insert ) {
           $results{$_} = $inref->{$_};
           push(@insertions, $inref->{$_}); 
        }

        # insert all fields for reservation into database
        $query = "INSERT INTO reservations VALUES ( " . join( ', ', ('?') x @insertions ) . " )";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, @insertions);
        if ( $results{'error_msg'} ) { return( 1, %results ); }

        $results{'id'} = $self->{'dbconn'}->{'dbh'}->{'mysql_insertid'};
    }
    $sth->finish();
    $results{'status_msg'} = 'Your reservation has been processed successfully. Your reservation ID number is ' . $results{'id'} . '.';
    return( 0, %results );
}


##### method delete_reservation
##### Deletes the reservation by setting the reservation status to cancelled.
sub delete_reservation
{
    my( $self, $inref ) = @_;

    return( $self->update_reservation($inref, $self->{'configs'}->{'CANCELLED'}) );
}


##### method get_reservations
### get the reservation list from the database and populate the table tag
# In: reference to hash of parameters
# Out: success or failure, and status message
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
        $query .= " FROM reservations WHERE user_dn = ? ORDER BY reservation_start_time";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    }

    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $rref = $sth->fetchall_arrayref({user_dn => 1, reservation_start_time => 2, reservation_end_time => 3, reservation_status => 4, src_hostaddrs_id => 5, dst_hostaddrs_id => 6, reservation_id => 7 });
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


##### sub get_reservation_detail
### get the reservation detail from the database
# In: reference to hash of parameters
# Out: success or failure, and status message
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
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'reservation_id'});
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

######################################################################
sub find_pending_reservations  {

    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg );
    my ( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_start_time < ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $status, $stime);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});
    # close it up
    $sth->finish();

    return( "", $data );
}


######################################################################
sub find_expired_reservations  {

    my ( $self, $stime, $status ) = @_;
    my ( $sth, $data, $query, $error_msg);
    my ( %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    #print "expired: Looking at time == " . $stime . "\n";

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_end_time < ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $status, $stime);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish();

    # return the answer
    return( "", $data );
}


##### method update_reservation
##### Updates reservation status.  Used to mark as active, finished, or cancelled.
######################################################################
sub update_reservation {

    my ( $self, $inref, $status ) = @_;
    my ( $sth, $query, %results );

    $results{'error_msg'} = $self->check_connection();
    if ($results{'error_msg'}) { return( 1, %results); }

    if ($status eq $self->{'configs'}->{'CANCELLED'}) {
            # This ensures that if the reservation is active, the LSP will be torn down the next time
            # find_expired_reservations runs.
        $query = qq{ UPDATE reservations SET reservation_end_time = 0 WHERE reservation_id = ?};
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'reservation_id'});
        if ( $results{'error_msg'} ) { return( 1, %results ); }
    }

    $query = qq{ UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?};
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $status, $inref->{'reservation_id'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # close it up
    $sth->finish();
    $results{'status_msg'} = "Successfully updated reservation.";
    return( 0, %results );
}


# private

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
    if (!$self->{'dbconn'}->{'dbh'}) { return( "Unable to make database connection"); }
    return "";
}

1;
