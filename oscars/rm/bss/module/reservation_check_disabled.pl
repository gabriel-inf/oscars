#!/usr/bin/perl

# reservation_check_disabled.pl:  DB handling for network
#                                 resource reservation process
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of mainstream #####

##### Beginning of sub routines #####

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
				return( 0, '[ERROR] An error has occurred while traversing the network path. Please try again later.' );
			}
			elsif ( $TempNodeLookupResult[0] eq 'non_abilene' )
			{
				return( 0, '[ERROR] This origin-destination path does not go through the Abilene network. Please check the origin and destination IP addresses and try again.' );
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
	my( $Dbh, $Sth, $Error_Status, $Query );

	my $Abilene_Conflict_Status = 0; # 0: no conflict, 1: existing conflict

	# TODO:  use LOCK_TABLES
	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		return( $Error_Status );
	}

	###
	# user level provisioning
	# if the user's level equals one of the read-only levels, don't let them submit a reservation
	#

	$Query = "SELECT $db_table_field_name{'users'}{'user_level'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
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

				return( 0, '[ERROR] Your user level (Lv. ' . $$Ref[0] . ') has a read-only privilege, and therefore you cannot make a new reservation request.' );
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

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		return( $Error_Status );
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
		( undef, $Error_Status ) = &Query_Execute( $Sth, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_Start_DateTime, $Comp_End_DateTime );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
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

		return( 0, '[ERROR] The available bandwidth limit on the Abilene network has been reached between ' . $Conflicted_Start_Time . ' UTC and ' . $Conflicted_End_Time . ' UTC. Please modify your reservation request and try again.' );
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

		( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			return( $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );

                        # TODO:  release lock

			$Error_Status =~ s/CantExecuteQuery\n//;
			return( 0, '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $Error_Status );
		}
		
		$New_Reservation_ID = $Dbh->{'mysql_insertid'};

		&Query_Finish( $Sth );
	}

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	### when everything has been processed successfully...
	# don't forget to show the user's new reservation ID
	return( 1, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
