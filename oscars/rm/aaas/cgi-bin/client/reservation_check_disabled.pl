#!/usr/bin/env perl

# reservation_check_disabled.pl:  Main interface CGI program for network
#                                 resource reservation process
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/database.pl';
require './lib/authenticate.pl';
require './lib/parseroute.pl';

# current script name
#$script_filename = 'reservation.pl';
$script_filename = $ENV{'SCRIPT_NAME'};

# template html file name for printing browser screen
$interface_template_filename = 'reservation.html';

# Abilene network bandwidth limit (in Mbps) (1 Gbps = 1000 Mbps)
$abilene_bandwidth_limit = 3000;

# global lock preference $use_lock is set in lib/general.pl
# don't forget to check other preference in lib/*.pl files as well

# Abilene network-related settings are in lib/parseroute.pl


##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'post' );

# check if the user is logged in
if ( &Verify_Login_Status( $user_login_cookie_name ) != 1 )
{
	# forward the user to the user login screen
	print "Location: $main_service_login_URI\n\n";
	exit;
}
else
{
	$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];
}

# if 'mode' eq 'reserve': Process reservation & print screen with result output
#                         (print screen subroutine is called at the end of reservation process)
# all else (default): Print screen for user input
if ( $FormData{'mode'} eq 'reserve' )
{
	&Process_Reservation();
}
else
{
	&Print_Interface_Screen();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Interface_Screen
# In: $Processing_Result [1 (success)/0 (fail)], $Processing_Result_Message
# Out: None (exits the program at the end)
sub Print_Interface_Screen
{

	my( $Processing_Result, $Processing_Result_Message );
	
	if ( $#_ >= 0 )
	{
		$Processing_Result = $_[0];
		$Processing_Result_Message = $_[1];
	}
	else
	{
		$Processing_Result = 1;
		$Processing_Result_Message = '';
	}
	
	# open html template file
	open( F_HANDLE, $interface_template_filename ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $interface_template_filename . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	my $Html_Line = join( '', @Template_Html );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach ( $Html_Line )
	{
		s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
		s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'loginname'}/g;
	}

	# if processing has failed for some reason, pre-fill the form with the %FormData values so that users do not need to fill the form again
	if ( $Processing_Result == 0 )
	{
		foreach $Key ( keys %FormData )
		{
			foreach ( $Html_Line )
			{
				s/(name="$Key")/$1 value="$FormData{$Key}"/ig;
			}
		}
	}

	print $Html_Line;

	exit;

}
##### End of sub Print_Interface_Screen


##### sub Process_Reservation
# In: None
# Out: None
# Calls sub Print_Interface_Screen at the end (with a success token)
sub Process_Reservation
{

	### check if browser had Javascript disabled
	# assume that most other data validations were done by the Javascript on client browser

	### validate origin & destination IP addresses
	# supports only the IPv4 format at this moment
	# [future note] it might be nice if we could check whether the IP address is actually reachable here...
	foreach $_ ( 'origin', 'destination' )
	{
		if ( $FormData{$_} !~ /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/ )
		{
			&Print_Interface_Screen( 0, '[ERROR] Please provide an IPv4 IP address for the ' . $_ . ' location.' );
		}
	}

	if ( $FormData{'origin'} eq $FormData{'destination'} )
	{
		&Print_Interface_Screen( 0, '[ERROR] Please provide different IP addresses for origin and destination locations.' );
	}

	# make bandwidth, date, and time values numeric
	foreach $_ ( 'bandwidth', 'start_year', 'start_month', 'start_date', 'start_hour', 'duration_hour' )
	{
		$FormData{$_} += 0;
	}

	# convert 12 am to 0 am
	if ( ( $FormData{'start_ampm'} eq 'am' ) && ( $FormData{'start_hour'} == 12 ) )
	{
		$FormData{'start_hour'} = 0;
	}

=head1
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
				&Print_Interface_Screen( 0, '[ERROR] An error has occurred while traversing the network path. Please try again later.' );
			}
			elsif ( $TempNodeLookupResult[0] eq 'non_abilene' )
			{
				&Print_Interface_Screen( 0, '[ERROR] This origin-destination path does not go through the Abilene network. Please check the origin and destination IP addresses and try again.' );
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

	# lock other database operations (check if there's any previous lock set)
	if ( $use_lock ne 'off' )
	{
		undef $Error_Status;

		$Error_Status = &Lock_Set();

		if ( $Error_Status != 1 )
		{
			&Print_Error_Screen( $script_filename, $Error_Status );
		}
	}

	# connect to the database
	undef $Error_Status;
	
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'loginname'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		foreach $ReadOnlyLevel ( @read_only_user_levels )
		{
			if ( $$Ref[0] eq $ReadOnlyLevel )
			{
				&Query_Finish( $Sth );
				&Database_Disconnect( $Dbh );

				if ( $use_lock ne 'off' )
				{
					&Lock_Release();
				}

				&Print_Interface_Screen( 0, '[ERROR] Your user level (Lv. ' . $$Ref[0] . ') has a read-only privilege, and therefore you cannot make a new reservation request.' );
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
		&Print_Error_Screen( $script_filename, $Error_Status );
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
			&Print_Error_Screen( $script_filename, $Error_Status );
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

		if ( $use_lock ne 'off' )
		{
			&Lock_Release();
		}

		&Print_Interface_Screen( 0, '[ERROR] The available bandwidth limit on the Abilene network has been reached between ' . $Conflicted_Start_Time . ' UTC and ' . $Conflicted_End_Time . ' UTC. Please modify your reservation request and try again.' );
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
			&Print_Error_Screen( $script_filename, $Error_Status );
		}

		( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );

			if ( $use_lock ne 'off' )
			{
				&Lock_Release();
			}

			$Error_Status =~ s/CantExecuteQuery\n//;
			&Print_Interface_Screen( 0, '[ERROR] An error has occurred while recording the reservation request on the database.<br>[Error] ' . $Error_Status );
		}
		
		$New_Reservation_ID = $Dbh->{'mysql_insertid'};

		&Query_Finish( $Sth );
	}

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# unlock the operation
	if ( $use_lock ne 'off' )
	{
		&Lock_Release();
	}

	### when everything has been processed successfully...
	# don't forget to show the user's new reservation ID
	&Print_Interface_Screen( 1, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
