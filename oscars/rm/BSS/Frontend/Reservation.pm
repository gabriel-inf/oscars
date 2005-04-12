package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(process_reservation);

use BSS::Frontend::Database;

#  network bandwidth limit (in Mbps) (1 Gbps = 1000 Mbps)
$bandwidth_limit = 3000;

##### Beginning of sub routines #####

# reservation:  contacts db to generate reservation

##### sub process_reservation
# In: reference to hash of parameters
# Out: success or failure, and status message
sub process_reservation
{
    ### check conflicts & record reservation
    # 1) schedule conflict; 2) path conflict; 3) bandwidth limit
    # perform lock before and after these processes

  my $conflict_status = 0; # 0: no conflict, 1: existing conflict

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # TODO:  lock table(s) with LOCK_TABLE

    ###
    # for each of the duration hours, grab the total amount of bandwidth
    # and see whether the requested amount plus the total of already reserved
    # amount exceeds the pre-set limit of bandwidth

  use DateTime;

    # prepare the query beforehand, and execute it within the foreach loop
    # Comparison rule:
    # ( Row.StartTime <= [Req.StartTime] AND Row.EndTime > [Req.StartTime] ) OR ( Row.StartTime > [Req.StartTime] AND Row.StartTime < [Req.EndTime] )
    # QUERY EXAMPLE: select SUM(bandwidth) from res where ( start <= '2004-09-11 02:00:00' and end > '2004-09-11 02:00:00' ) or ( start > '2004-09-11 02:00:00' and start < '2004-09-11 03:00:00' );
	
  $query = "SELECT SUM($table_field{'reservations'}{'qos'}) FROM $table{'reservations'} WHERE ( $table_field{'reservations'}{'start_time'} <= ? AND $table_field{'reservations'}{'end_time'} > ? ) OR ( $table_field{'reservations'}{'start_time'} > ? AND $table_field{'reservations'}{'start_time'} < ? )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # to show the information on the error screen if conflic occurs...
  my( $Conflicted_Start_Time, $Conflicted_End_Time );

  DURATION: foreach $_ ( 0 .. ( $args_ref->{'duration_hour'} - 1 ) )
  {
      my %temp_date_time;
      @temp_date_time{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
      my $dt_comp_start = DateTime->new( %temp_date_time );
      $dt_comp_start->add( hours => $_ );

        # now get the comparison end time (one hour later)
      my $dt_comp_end = $dt_comp_start->clone->add( hours => ( $_ + 1 ) );

        # 'dbinput' type uses gmtime( epoch_time ); hence making everything converted to UTC
      my $comp_start_datetime = &Create_Time_String( 'dbinput', $dt_comp_start->epoch );
      my $comp_end_datetime = &Create_Time_String( 'dbinput', $dt_comp_end->epoch );

        # execute query with the comparison start & end datetime strings
        # the order is: [Req.StartTime], [Req.StartTime], [Req.StartTime], [Req.EndTime]
      ( $error_code, undef ) = query_execute( $sth, $comp_start_datetime, $comp_start_datetime, $comp_start_datetime, $comp_end_datetime );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }

      my $db_bandwidth_sum;
      while ( my $ref = $sth->fetchrow_arrayref ) {
          $db_bandwidth_sum = $$ref[0];
      }

        # Check the bandwidth limit; if the existing total plus the requested
        # amount exceeds the pre-set limit, # set the conflict status to 1 and
        # exit the loop
      if ( ( $db_bandwidth_sum + $args_ref->{'bandwidth'} ) > $abilene_bandwidth_limit ) {
          $conflict_status = 1;
          ( $Conflicted_Start_Time, $Conflicted_End_Time ) = ( $comp_start_datetime, $comp_end_datetime );
          last DURATION;
      }
  } # end of DURATION loop

  query_finish( $sth );

    # if the conflict status is 0, record the reservation to the database
    # otherwise, return error message

  my $new_reservation_id unless ( $conflict_status );
  if ( $conflict_status ) {
                # TODO:  unlock table(s)
      database_disconnect( $dbh );
      return( 1, '[ERROR] The available bandwidth limit on the network has been reached between ' . $Conflicted_Start_Time . ' UTC and ' . $Conflicted_End_Time . ' UTC. Please modify your reservation request and try again.' );
  }
  else {
        # get the reservation start time
      my %temp_date_time;
      @temp_date_time{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
      my $dtResStart = DateTime->new( %temp_date_time );

        # now get the reservation end time
      my $dtResEnd = $dtResStart->clone->add( hours => $args_ref->{'duration_hour'} );

        # 'dbinput' type uses gmtime( epoch_time ); hence convert everything to UTC
      my $start_time = &Create_Time_String( 'dbinput', $dtResStart->epoch );
      my $end_time = &Create_Time_String( 'dbinput', $dtResEnd->epoch );
      my $current_time = &Create_Time_String( 'dbinput', time );

      my @stuffs_to_insert = ( '', @FormData{ 'loginname', 'origin', 'destination', 'bandwidth' }, $start_time, $end_time, $args_ref->{'description'}, $current_time, @ENV{ 'REMOTE_ADDR', 'REMOTE_HOST', 'HTTP_USER_AGENT' } );

        # insert into database query statement
      $query = "INSERT INTO $table{'reservations'} VALUES ( " . join( ', ', ('?') x @stuffs_to_insert ) . " )";

      ( $error_code, $sth ) = query_prepare( $dbh, $query );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }

      ( $error_code, undef ) = query_execute( $sth, @stuffs_to_insert );
      if ( $error_code ) {
              # TODO:  unlock tables
          database_disconnect( $dbh );
          $error_code =~ s/CantExecuteQuery\n//;
          return( 1, '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $error_code );
      }
		
      $new_reservation_id = $dbh->{'mysql_insertid'};

      query_finish( $sth );
  }
	# TODO:  unlock tables
  database_disconnect( $dbh );
  return( 0, 'Your reservation has been processed successfully. Your reservation ID number is ' . $new_reservation_id . '.' );
}


# reservationlist:  Reservation List DB handling

##### sub get_reservations_list
### get the reservation list from the database and populate the table tag
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservations_list
{
  my( $dbh, $sth, $error_code, $query );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return(1, $error_code ); }

    # DB query: get the reservation list
    # CAUTION: do not change the elements order of this array!!
  my @fields_to_read = ( 'id', 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'qos', 'start_time', 'end_time' );

  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      $query .= $table_field{'reservations'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
    # sort by reservation ID in descending order
  $query .= " FROM $table{'reservations'} ORDER BY $table_field{'reservations'}{'reservation_id'} DESC";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return(1, $error_code );
  }

  ( $error_code, undef ) = query_execute( $sth );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return(1, $error_code );
  }

    # populate %reservations_data with the data fetched from the database
  my %reservations_data;
  @reservations_data{@fields_to_read} = ();
  $sth->bind_columns( map { \$reservations_data{$_} } @fields_to_read );

  my $reservation_list_table;

  while ( $sth->fetch() ) {
      my @resv_list_table_row;

        # iterate through @fields_to_read
      foreach $_ ( 0 .. $#fields_to_read ) {
            # TODO:  FIX
            # If the cell content is Reservation ID, surround it with a link
            # to the detailed info page.  The javascript function
            #  'open_resv_detail_window' is defined in ./reservationlist.js
          if ( $fields_to_read[$_] eq 'reservation_id' ) {
              push( @resv_list_table_row, '<td><a href="#" onClick="javascript:open_resv_detail_window(\'?mode=resvdetail&resvid=' . $reservations_data{$fields_to_read[$_]} . '\');">' . $reservations_data{$fields_to_read[$_]} . '</a></td>' );
          }
          elsif ( $fields_to_read[$_] eq 'qos' ) {
              push( @resv_list_table_row, "<td>$reservations_data{$fields_to_read[$_]} Mbps</td>" );
          }
          else {
              push( @resv_list_table_row, "<td>$reservations_data{$fields_to_read[$_]}</td>" );
          }
      }

        # the second column of the table is reservation requester's login name
      if ( $resv_list_table_row[1] =~ /<td>([^<]+)<\/td>/ ) {
          if ( $1 eq $args_ref->{'loginname'} ) {
                # highlight the row if login name matches that of the
                # currently logged-in user's
              unshift( @resv_list_table_row, '<tr class="attention">' );
          }
          else {
                # do not highlight the row
              unshift( @resv_list_table_row, '<tr>' );
          }
      }
      push( @resv_list_table_row, "</tr>\n" );
		
      $reservation_list_table .= join( '', @resv_list_table_row );
  }
  query_finish( $sth );
  database_disconnect( $dbh );
  return (0, 'success');
}


##### sub get_reservation_detail
### get the reservation detail from the database
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservation_detail
{
  my( $dbh, $sth, $error_code, $query );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return(1, $error_code ); }

    # names of the fields to be displayed on the screen
  my @fields_to_display = ( 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'qos', 'start_time', 'end_time', 'description', 'created_time' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_display ) {
      $query .= $table_field{'reservations'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $table{'reservations'} WHERE $table_field{'reservations'}{'reservation_id'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return(1, $error_code );
  }

  ( $error_code, undef ) = query_execute( $sth, $args_ref->{'resvid'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return(1, $error_code );
  }

    # populate %reservations_data with the data fetched from the database
  my %reservations_data;
  @reservations_data{@fields_to_display} = ();
  $sth->bind_columns( map { \$reservations_data{$_} } @fields_to_display );
  $sth->fetch();

  query_finish( $sth );
  database_disconnect( $dbh );
  return (0, 'success');
}

1;
