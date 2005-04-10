#!/usr/bin/perl

# reservation.pl:  contacts db to generate reservation
# Last modified: April 9, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'lib/general.pl';
require 'lib/database.pl';

#  network bandwidth limit (in Mbps) (1 Gbps = 1000 Mbps)
$bandwidth_limit = 3000;



##### Beginning of sub routines #####

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


##### End of sub routines #####

##### End of script #####
