package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(get_reservations process_reservation);

use BSS::Frontend::Database;

#  network bandwidth limit (in Mbps) (1 Gbps = 1000 Mbps)
$bandwidth_limit = 3000;

##### Beginning of sub routines #####

# from reservation.pl:  contacts db to generate reservation

##### sub process_reservation
# In: reference to hash of parameters
# Out: success or failure, and status message
sub process_reservation
{
  my($args_href) = @_;
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }
  my $over_limit = 0; # whether any time segment is over the bandwidth limit
  my $end_time = args_href->{'start_time'} + args_href->{'duration'};

    # TODO:  lock table(s) with LOCK_TABLE

    ###
    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
  $query = "SELECT $table_field{'reservations'}{'qos'}, $table_field{'reservations'}{'start_time'}, $table_field{'reservations'}{'end_time'} FROM $table{'reservations'} WHERE ( $table_field{'reservations'}{'end_time'} >= ? AND $table_field{'reservations'}{'start_time'} <= ? )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }
      # execute query with the comparison start & end datetime strings
  ( $error_code, $rows ) = query_execute( $sth, $args_href->{'start_time'}, $end_time} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  # TODO:  find segments of overlap, determine if bandwidth in any is
  #        over the limit; return time segments if error
  query_finish( $sth );

  # TODO:  unlock table(s)

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.

  my $new_reservation_id unless ( $over_limit );
  if ( $over_limit ) {
      database_disconnect( $dbh );
          # TODO:  list of times
      return( 1, '[ERROR] The available bandwidth limit on the network has been reached between '. 'Please modify your reservation request and try again.' );
  }
  else {
      my @stuffs_to_insert = ( '', @FormData{ 'loginname', 'origin', 'destination', 'bandwidth' }, args_href->{'start_time'}, $end_time, $args_ref->{'description'}, $current_time, @ENV{ 'REMOTE_ADDR', 'REMOTE_HOST', 'HTTP_USER_AGENT' } );

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


# from reservationlist.pl:  Reservation List DB handling

##### sub get_reservations
### get the reservation list from the database and populate the table tag
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservations
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
