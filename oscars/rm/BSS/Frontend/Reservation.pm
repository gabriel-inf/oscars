package BSS::Frontend::Reservation;

# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use BSS::Frontend::General;
use BSS::Frontend::Database;

#  network bandwidth limit (in Mbps) (1 Gbps = 1000 Mbps)
$bandwidth_limit = 3000;

##### Beginning of sub routines #####

# reservation:  contacts db to generate reservation

##### sub Process_Reservation
# In: Soap array
# Out: success or failure, and reservation info
sub Process_Reservation
{

	### check conflicts & record reservation
	# 1) schedule conflict; 2) path conflict; 3) bandwidth limit
	# perform lock before and after these processes

	my $Conflict_Status = 0; # 0: no conflict, 1: existing conflict

	# TODO:  lock table(s) with LOCK_TABLE

	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}


	###
	# for each of the duration hours, grab the total amount of bandwidth
	# and see whether the requested amount plus the total of already reserved amount exceeds the pre-set limit of bandwidth
	#

	# for date/time calculation
	use DateTime;

	# prepare the query beforehand, and execute it within the foreach loop
	# Comparison rule:
	# ( Row.StartTime <= [Req.StartTime] AND Row.EndTime > [Req.StartTime] ) OR ( Row.StartTime > [Req.StartTime] AND Row.StartTime < [Req.EndTime] )
	# QUERY EXAMPLE: select SUM(bandwidth) from res where ( start <= '2004-09-11 02:00:00' and end > '2004-09-11 02:00:00' ) or ( start > '2004-09-11 02:00:00' and start < '2004-09-11 03:00:00' );
	
	$Query = "SELECT SUM($db_table_field_name{'reservations'}{'reserv_bandwidth'}) FROM $db_table_name{'reservations'} WHERE ( $db_table_field_name{'reservations'}{'reserv_start_time'} <= ? AND $db_table_field_name{'reservations'}{'reserv_end_time'} > ? ) OR ( $db_table_field_name{'reservations'}{'reserv_start_time'} > ? AND $db_table_field_name{'reservations'}{'reserv_start_time'} < ? )";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# to show the information on the error screen if conflic occurs...
	my( $Conflicted_Start_Time, $Conflicted_End_Time );

	DURATION: foreach $_ ( 0 .. ( $FormData{'duration_hour'} - 1 ) )
	{
		my %TempDateTime;
		@TempDateTime{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
		my $dtCompStart = DateTime->new( %TempDateTime );
		$dtCompStart->add( hours => $_ );

		# now get the comparison end time (one hour later)
		my $dtCompEnd = $dtCompStart->clone->add( hours => ( $_ + 1 ) );

		# 'dbinput' type uses gmtime( epoch_time ); hence making everything converted to UTC
		my $Comp_Start_DateTime = &Create_Time_String( 'dbinput', $dtCompStart->epoch );
		my $Comp_End_DateTime = &Create_Time_String( 'dbinput', $dtCompEnd->epoch );

		# execute query with the comparison start & end datetime strings
		# the order is: [Req.StartTime], [Req.StartTime], [Req.StartTime], [Req.EndTime]
		( $Error_Code, undef ) = &Query_Execute( $Sth, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_End_DateTime );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		my $DB_Bandwidth_Sum;

		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			$DB_Bandwidth_Sum = $$Ref[0];
		}

		# check the bandwidth limit
		# if the existing total plus the requested amount exceeds the pre-set limit,
		# set the conflict status to 1 and exit the loop
		if ( ( $DB_Bandwidth_Sum + $FormData{'bandwidth'} ) > $abilene_bandwidth_limit )
		{
			$Conflict_Status = 1;
			( $Conflicted_Start_Time, $Conflicted_End_Time ) = ( $Comp_Start_DateTime, $Comp_End_DateTime );
			last DURATION;
		}
	} # end of DURATION loop

	&Query_Finish( $Sth );

	###
	# if the conflict status is 0, record the reservation to the database
	# otherwise, print error message on the screen
	#

	my $New_Reservation_ID unless ( $Conflict_Status );

	if ( $Conflict_Status )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  unlock table(s)

		return( 1, '[ERROR] The available bandwidth limit on the network has been reached between ' . $Conflicted_Start_Time . ' UTC and ' . $Conflicted_End_Time . ' UTC. Please modify your reservation request and try again.' );
	}
	else
	{
		# get the reservation start time
		my %TempDateTime;
		@TempDateTime{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
		my $dtResStart = DateTime->new( %TempDateTime );

		# now get the reservation end time
		my $dtResEnd = $dtResStart->clone->add( hours => $FormData{'duration_hour'} );

		# 'dbinput' type uses gmtime( epoch_time ); hence convert everything to UTC
		my $Resv_Start_DateTime = &Create_Time_String( 'dbinput', $dtResStart->epoch );
		my $Resv_End_DateTime = &Create_Time_String( 'dbinput', $dtResEnd->epoch );
		my $Current_DateTime = &Create_Time_String( 'dbinput', time );

		my @Stuffs_to_Insert = ( '', @FormData{ 'loginname', 'origin', 'destination', 'bandwidth' }, $Resv_Start_DateTime, $Resv_End_DateTime, $FormData{'description'}, $Current_DateTime, @ENV{ 'REMOTE_ADDR', 'REMOTE_HOST', 'HTTP_USER_AGENT' } );

		# insert into database query statement
		$Query = "INSERT INTO $db_table_name{'reservations'} VALUES ( " . join( ', ', ('?') x @Stuffs_to_Insert ) . " )";

		( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		( $Error_Code, undef ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );

                        # TODO:  unlock tables

			$Error_Code =~ s/CantExecuteQuery\n//;
			return( 1, '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $Error_Code );
		}
		
		$New_Reservation_ID = $Dbh->{'mysql_insertid'};

		&Query_Finish( $Sth );
	}

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# TODO:  unlock tables

	### when everything has been processed successfully...
	# don't forget to show the user's new reservation ID
	return( 0, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


# reservation_check_disabled:  DB handling for network
#                                 resource reservation process

##### sub Process_Reservation
# In: FormData
# Out: None
sub Process_Reservation(FormData)
{
	### see if the origin-destination path passes through Abilene; if not, do not accept reservation
	# look up the closest Abilene node to each host (origin/destination)
	# if the node does not exist, the network path does not go past Abilene
	my %Closest_Abilene_Node;

	# origin first; and then, destination (do not change the order of keys below!)
	foreach $Key ( 'origin', 'destination' )
	{
		my @TempNodeLookupResult;

		if ( $Key eq 'origin' )
		{
			# connect to the Indianapolis node (which is the closest to the login server)
			# and run traceroute to the origin to find out the closest Abilene node to the origin
			@TempNodeLookupResult = &Abilene_Node_Lookup( 'ipls', $FormData{$Key} );
		}
		else
		{
			# connect to the Abilene node that is closest to the origin
			# and run traceroute from that node to the destination host
			@TempNodeLookupResult = &Abilene_Node_Lookup( $Closest_Abilene_Node{'origin'}, $FormData{$Key} );
		}

		if ( $TempNodeLookupResult[0] != 1 )
		{
			if ( $TempNodeLookupResult[0] eq 'command_fail' )
			{
				return( 1, '[ERROR] An error has occurred while traversing the network path. Please try again later.' );
			}
			elsif ( $TempNodeLookupResult[0] eq 'non_abilene' )
			{
				return( 1, '[ERROR] This origin-destination path does not go through the Abilene network. Please check the origin and destination IP addresses and try again.' );
			}
		}
		else
		{
			$Closest_Abilene_Node{$Key} = $TempNodeLookupResult[1];
		}
	}
=cut

	### check conflicts & record reservation
	# 1) schedule conflict; 2) path conflict; 3) bandwidth limit
	# perform lock before and after these processes

	### [notes on 6/14/2004]
	# [note] look at 1) and 3) only, since we now treat the entire Abilene network as a single path
	# let's not take individual path bandwidth/schedule conflict into account for now
	# because network route can change at any moment, and strict path control is not a very good thing to do

	### start working with the database
	my( $Dbh, $Sth, $Error_Code, $Query );

	my $Abilene_Conflict_Status = 0; # 0: no conflict, 1: existing conflict

	# TODO:  use LOCK_TABLES
	# connect to the database
	undef $Error_Code;
	
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return( 1, $Error_Code );
	}

	###
	# user level provisioning
	# if the user's level equals one of the read-only levels, don't let them submit a reservation
	#

	$Query = "SELECT $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	( $Error_Code, undef ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		foreach $ReadOnlyLevel ( @read_only_user_levels )
		{
			if ( $$Ref[0] eq $ReadOnlyLevel )
			{
				&Query_Finish( $Sth );
				&Database_Disconnect( $Dbh );

                                # TODO:  release lock

				return( 1, '[ERROR] Your user level (Lv. ' . $$Ref[0] . ') has a read-only privilege, and therefore you cannot make a new reservation request.' );
			}
		}
	}

	&Query_Finish( $Sth );

	###
	# for each of the duration hours, grab the total amount of bandwidth
	# and see whether the requested amount plus the total of already reserved amount exceeds the pre-set limit of bandwidth
	#

	# for date/time calculation
	use DateTime;

	# prepare the query beforehand, and execute it within the foreach loop
	# Comparison rule:
	# ( Row.StartTime <= [Req.StartTime] AND Row.EndTime > [Req.StartTime] ) OR ( Row.StartTime > [Req.StartTime] AND Row.StartTime < [Req.EndTime] )
	# QUERY EXAMPLE: select SUM(bandwidth) from res where ( start <= '2004-09-11 02:00:00' and end > '2004-09-11 02:00:00' ) or ( start > '2004-09-11 02:00:00' and start < '2004-09-11 03:00:00' );
	
	$Query = "SELECT SUM($db_table_field_name{'reservations'}{'reserv_bandwidth'}) FROM $db_table_name{'reservations'} WHERE ( $db_table_field_name{'reservations'}{'reserv_start_time'} <= ? AND $db_table_field_name{'reservations'}{'reserv_end_time'} > ? ) OR ( $db_table_field_name{'reservations'}{'reserv_start_time'} > ? AND $db_table_field_name{'reservations'}{'reserv_start_time'} < ? )";

	( $Error_Code, $Sth) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return( 1, $Error_Code );
	}

	# to show the information on the error screen if conflic occurs...
	my( $Conflicted_Start_Time, $Conflicted_End_Time );

	DURATION: foreach $_ ( 0 .. ( $FormData{'duration_hour'} - 1 ) )
	{
		my %TempDateTime;
		@TempDateTime{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
		my $dtCompStart = DateTime->new( %TempDateTime );
		$dtCompStart->add( hours => $_ );

		# now get the comparison end time (one hour later)
		my $dtCompEnd = $dtCompStart->clone->add( hours => ( $_ + 1 ) );

		# 'dbinput' type uses gmtime( epoch_time ); hence making everything converted to UTC
		my $Comp_Start_DateTime = &Create_Time_String( 'dbinput', $dtCompStart->epoch );
		my $Comp_End_DateTime = &Create_Time_String( 'dbinput', $dtCompEnd->epoch );

		# execute query with the comparison start & end datetime strings
		# the order is: [Req.StartTime], [Req.StartTime], [Req.StartTime], [Req.EndTime]
		( $Error_Code, undef ) = &Query_Execute( $Sth, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_End_DateTime );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		my $DB_Bandwidth_Sum;

		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			$DB_Bandwidth_Sum = $$Ref[0];
		}

		# check the Abilene bandwidth limit
		# if the existing total plus the requested amount exceeds the pre-set limit,
		# set the conflict status to 1 and exit the loop
		if ( ( $DB_Bandwidth_Sum + $FormData{'bandwidth'} ) > $abilene_bandwidth_limit )
		{
			$Abilene_Conflict_Status = 1;
			( $Conflicted_Start_Time, $Conflicted_End_Time ) = ( $Comp_Start_DateTime, $Comp_End_DateTime );
			last DURATION;
		}
	} # end of DURATION loop

	&Query_Finish( $Sth );

	###
	# if the conflict status is 0, record the reservation to the database
	# otherwise, print error message on the screen
	#

	my $New_Reservation_ID unless ( $Abilene_Conflict_Status );

	if ( $Abilene_Conflict_Status )
	{
		&Database_Disconnect( $Dbh );

                # TODO:  release lock

		return( 1, '[ERROR] The available bandwidth limit on the Abilene network has been reached between ' . $Conflicted_Start_Time . ' UTC and ' . $Conflicted_End_Time . ' UTC. Please modify your reservation request and try again.' );
	}
	else
	{
		# get the reservation start time
		my %TempDateTime;
		@TempDateTime{ 'year', 'month', 'day', 'hour', 'time_zone' } = @FormData{ 'start_year', 'start_month', 'start_date', 'start_hour', 'start_timeoffset' };
		my $dtResStart = DateTime->new( %TempDateTime );

		# now get the reservation end time
		my $dtResEnd = $dtResStart->clone->add( hours => $FormData{'duration_hour'} );

		# 'dbinput' type uses gmtime( epoch_time ); hence convert everything to UTC
		my $Resv_Start_DateTime = &Create_Time_String( 'dbinput', $dtResStart->epoch );
		my $Resv_End_DateTime = &Create_Time_String( 'dbinput', $dtResEnd->epoch );
		my $Current_DateTime = &Create_Time_String( 'dbinput', time );

		my @Stuffs_to_Insert = ( '', @FormData{ 'loginname', 'origin', 'destination', 'bandwidth' }, $Resv_Start_DateTime, $Resv_End_DateTime, $FormData{'description'}, $Current_DateTime, @ENV{ 'REMOTE_ADDR', 'REMOTE_HOST', 'HTTP_USER_AGENT' } );

		# insert into database query statement
		$Query = "INSERT INTO $db_table_name{'reservations'} VALUES ( " . join( ', ', ('?') x @Stuffs_to_Insert ) . " )";

		( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );
			return( 1, $Error_Code );
		}

		( $Error_Code, undef ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
		if ( $Error_Code )
		{
			&Database_Disconnect( $Dbh );

                        # TODO:  release lock

			$Error_Code =~ s/CantExecuteQuery\n//;
			return( 1, '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $Error_Code );
		}
		
		$New_Reservation_ID = $Dbh->{'mysql_insertid'};

		&Query_Finish( $Sth );
	}

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	### when everything has been processed successfully...
	# don't forget to show the user's new reservation ID
	return( 0, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


# reservationlist:  Reservation List DB handling

##### sub Get_Reservations_List
# In: FormData
# Out: None (exits the program at the end)
sub Get_Reservations_list(FormData)
{
	### get the reservation list from the database and populate the table tag
	my( $Dbh, $Sth, $Error_Code, $Query );

	# connect to the database
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return(1, $Error_Code );
	}

	# DB Query: get the reservation list
	# CAUTION: do not change the elements order of this array!!
	my @Fields_to_Read = ( 'reservation_id', 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'reserv_bandwidth', 'reserv_start_time', 'reserv_end_time' );

	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		$Query .= $db_table_field_name{'reservations'}{$_} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	# sort by reservation ID in descending order
	$Query .= " FROM $db_table_name{'reservations'} ORDER BY $db_table_field_name{'reservations'}{'reservation_id'} DESC";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return(1, $Error_Code );
	}

	( $Error_Code, undef ) = &Query_Execute( $Sth );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return(1, $Error_Code );
	}

	# populate %Reservations_Data with the data fetched from the database
	my %Reservations_Data;
	@Reservations_Data{@Fields_to_Read} = ();
	$Sth->bind_columns( map { \$Reservations_Data{$_} } @Fields_to_Read );

	my $Reservation_List_Table;

	while ( $Sth->fetch() )
	{
		my @Resv_List_Table_Row;

		# iterate through @Fields_to_Read
		foreach $_ ( 0 .. $#Fields_to_Read )
		{
			# if the cell content is Reservation ID, surround it with a link to the detailed info page
			# the javascript function 'open_resv_detail_window' is defined in ./reservationlist.js
			if ( $Fields_to_Read[$_] eq 'reservation_id' )
			{
				push( @Resv_List_Table_Row, '<td><a href="#" onClick="javascript:open_resv_detail_window(\'?mode=resvdetail&resvid=' . $Reservations_Data{$Fields_to_Read[$_]} . '\');">' . $Reservations_Data{$Fields_to_Read[$_]} . '</a></td>' );
			}
			elsif ( $Fields_to_Read[$_] eq 'reserv_bandwidth' )
			{
				push( @Resv_List_Table_Row, "<td>$Reservations_Data{$Fields_to_Read[$_]} Mbps</td>" );
			}
			else
			{
				push( @Resv_List_Table_Row, "<td>$Reservations_Data{$Fields_to_Read[$_]}</td>" );
			}
		}

		# the second column of the table is reservation requester's login name
		if ( $Resv_List_Table_Row[1] =~ /<td>([^<]+)<\/td>/ )
		{
			if ( $1 eq $FormData{'loginname'} )
			{
				# highlight the row if login name matches that of the currently logged-in user's
				unshift( @Resv_List_Table_Row, '<tr class="attention">' );
			}
			else
			{
				# do not highlight the row
				unshift( @Resv_List_Table_Row, '<tr>' );
			}
		}

		push( @Resv_List_Table_Row, "</tr>\n" );
		
		$Reservation_List_Table .= join( '', @Resv_List_Table_Row );
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

        return (0, 'success');
	exit;

}
##### End of sub Get_Reservations_List


##### sub Get_Reservation_Detail
# In: FormData
# Out: None (exits the program at the end)
sub Print_Reservation_Detail(FormData)
{
	### get the reservation detail from the database
	my( $Dbh, $Sth, $Error_Code, $Query );

	# connect to the database
	( $Error_Code, $Dbh ) = &Database_Connect();
	if ( $Error_Code )
	{
		return(1, $Error_Code );
	}

	# names of the fields to be displayed on the screen
	my @Fields_to_Display = ( 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'reserv_bandwidth', 'reserv_start_time', 'reserv_end_time', 'reserv_description', 'reserv_made_time' );

	# DB Query: get the user profile detail
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Display )
	{
		$Query .= $db_table_field_name{'reservations'}{$_} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	$Query .= " FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'reservation_id'} = ?";

	( $Error_Code, $Sth ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return(1, $Error_Code );
	}

	( $Error_Code, undef ) = &Query_Execute( $Sth, $FormData{'resvid'} );
	if ( $Error_Code )
	{
		&Database_Disconnect( $Dbh );
		return(1, $Error_Code );
	}

	# populate %Reservations_Data with the data fetched from the database
	my %Reservations_Data;
	@Reservations_Data{@Fields_to_Display} = ();
	$Sth->bind_columns( map { \$Reservations_Data{$_} } @Fields_to_Display );
	$Sth->fetch();

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

        return (0, 'success');
}
##### End of sub Get_Reservation_Detail

1;
