package BSS::Frontend::Reservation;

# Reservation.pm:
# Last modified: April 18, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(get_reservations insert_reservation delete_reservation);

use DB;
use BSS::Frontend::Database;



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
  my( $inref ) = @_;
  my( $dbh, $query, $sth );
  my( %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }
  my $over_limit = 0; # whether any time segment is over the bandwidth limit

    ###
    # Get bandwidth and times of reservations overlapping that of the
    # reservation request.
  $query = "SELECT $Table_field{'reservations'}{'bandwidth'}, $Table_field{'reservations'}{'start_time'}, $Table_field{'reservations'}{'end_time'} FROM $Table{'reservations'} WHERE ( $Table_field{'reservations'}{'end_time'} >= ? AND $Table_field{'reservations'}{'start_time'} <= ? )";

  $inref->{'created_time'} = '';  # only holds a time if reservation successful

      # handled query with the comparison start & end datetime strings
  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'reservations'}, READ_LOCK, $inref->{'start_time'}, $inref->{'end_time'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

  # TODO:  find segments of overlap, determine if bandwidth in any is
  #        over the limit; return time segments if error

    # If no segment is over the limit,  record the reservation to the database.
    # otherwise, return error message (TODO) with the times involved.

  #$results{'id'} unless ( $over_limit );
  if ( $over_limit ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'reservations'});
          # TODO:  list of times
      results{'error_msg'} = '[ERROR] The available bandwidth limit on the network has been reached between '. 'Please modify your reservation request and try again.';
      return( 1, %results );
  }
  else {
      query_finish( $sth );
      database_unlock_table( $Table{'reservations'} );

          # get interface id's from edge router ip's
      $inref->{'ingress_id'} = ipaddr_to_iface_idx($inref->{'ingress_router'}); 
      $inref->{'egress_id'} = ipaddr_to_iface_idx($inref->{'egress_router'}); 

          # get ipaddr id from host's and destination's ip addresses
      $inref->{'src_id'} = hostaddr_to_idx($inref->{'src_ip'}); 
      $inref->{'dst_id'} = hostaddr_to_idx($inref->{'dst_ip'}); 
      $inref->{'created_time'} = time();

      my @insertions;   # copy over input fields that will be set in table
      foreach $_ ( @Table_field_order ) {
         $results{$_} = $inref->{$_};
         push(@insertions, $inref->{$_}); 
      }

        # insert all fields for reservation into database
      $query = "INSERT INTO $Table{'reservations'} VALUES ( " . join( ', ', ('?') x @insertions ) . " )";

      ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'reservations'}, READ_LOCK, @insertions);
      if ( $results{'error_msg'} )
      {
          $results{'error_msg'} =~ "s/CantExecuteQuery\n//";
          $results{'error_msg'} = '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $results{'error_msg'};
           return( 1, %results );
      }
		
      $results{'id'} = $dbh->{'mysql_insertid'};
  }
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'reservations'});
  $results{'status_msg'} = 'Your reservation has been processed successfully. Your reservation ID number is ' . $results{'id'} . '.';
  return( 0, %results );
}


# from reservationlist.pl:  Reservation List DB handling

##### sub get_reservations
### get the reservation list from the database and populate the table tag
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservations
{
  my( $inref ) = @_;
  my( $dbh, $sth, $query );
  my( %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return(1, %results ); }

    # DB query: get the reservation list
    # CAUTION: do not change the elements order of this array!!
  my @fields_to_read = ( 'id', 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'bandwidth', 'start_time', 'end_time' );

  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      $query .= $Table_field{'reservations'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
    # sort by reservation ID in descending order
  $query .= " FROM $Table{'reservations'} ORDER BY $Table_field{'reservations'}{'reservation_id'} DESC";

  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'reservations'}, READ_LOCK);
  if ( $results{'error_msg'} ) { return(1, %results ); }

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
          elsif ( $fields_to_read[$_] eq 'bandwidth' ) {
              push( @resv_list_table_row, "<td>$reservations_data{$fields_to_read[$_]} Mbps</td>" );
          }
          else {
              push( @resv_list_table_row, "<td>$reservations_data{$fields_to_read[$_]}</td>" );
          }
      }

        # the second column of the table is reservation requester's login name
      if ( $resv_list_table_row[1] =~ /<td>([^<]+)<\/td>/ ) {
          if ( $1 eq $inref->{'loginname'} ) {
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
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'reservations'});
  results{'status_msg'} = 'Successfully read reservations';
  return( 0, %results );
}


    # stub
sub delete_reservation
{
}


##### sub get_reservation_detail
### get the reservation detail from the database
# In: reference to hash of parameters
# Out: success or failure, and status message
sub get_reservation_detail
{
  my( $inref ) = @_;
  my( $dbh, $sth, $query );
  my( %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return(1, %results ); }

    # names of the fields to be displayed on the screen
  my @fields_to_display = ( 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'bandwidth', 'start_time', 'end_time', 'description', 'created_time' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_display ) {
      $query .= $Table_field{'reservations'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'reservations'} WHERE $Table_field{'reservations'}{'reservation_id'} = ?";
  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'reservations'}, READ_LOCK, $inref->{'id'});

  if ( $results{'error_msg'} ) { return(1, %results ); }

    # populate %reservations_data with the data fetched from the database
  my %reservations_data;
  @reservations_data{@fields_to_display} = ();
  $sth->bind_columns( map { \$reservations_data{$_} } @fields_to_display );
  $sth->fetch();

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'reservations'});
  $results{'status_msg'} = 'Successfully got reservation details.';
  return (0, %results);
}

1;
