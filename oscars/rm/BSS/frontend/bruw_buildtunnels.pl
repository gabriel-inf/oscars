#!/usr/bin/env perl

# bruw_buildtunnels.pl
#
# Back-end scheduler: Go through the reservation database and build MPLS tunnels
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib_general.cgi';
require '../lib_database.cgi';
require '../lib_parseroute.cgi';
require '../lib_abilene_mpls_setup.cgi';

# incldue environment settings
require '../configs/config_network.cgi';
require '../configs/config_sendmail.cgi';

# current script name
$script_filename = 'bruw_buildtunnels.cgi';

# set AUTOFLUSH to true
$| = 1;

##### Beginning of mainstream #####

### task order: build new tunnels
### this script is supposed to run 5-10 minutes before every hour

### read 'reservations' table from the database
### and get the list of tunnels to build

	### for date/time calculation
	use DateTime;

	# get the next hour and next+1 hour
	my $dtNxtHr = DateTime->now()->truncate( to => 'hour' )->add( hours => 1 );
	my $dtNxtp1Hr = DateTime->now()->truncate( to => 'hour' )->add( hours => 2 );

	# 'dbinput' type uses gmtime( epoch_time ); hence convert the time to an offset from the epoch using the $dt->epoch() method
	my $Next_Hour_DateTime = &Create_Time_String( 'dbinput', $dtNxtHr->epoch() );
	my $Next_Hour_p1_DateTime = &Create_Time_String( 'dbinput', $dtNxtp1Hr->epoch() );

	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# name of the database fields to be retrieved
	# CAUTION: 'reservation_id' should always be the first element!
	my @Fields_to_Read = ( 'reservation_id', 'user_loginname', 'reserv_origin_ip', 'reserv_dest_ip', 'reserv_bandwidth', 'reserv_end_time' );

	### DB Query: get the reservation information of those that are
	# ( Res.StartTime >= [NxtHr] ) AND ( Res.StartTime < [Nxtp1Hr] )
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		$Query .= $db_table_field_name{'reservations'}{$_} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	# datetime conditions
	$Query .= " FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'reserv_start_time'} >= ? AND $db_table_field_name{'reservations'}{'reserv_start_time'} < ?";

	# prepare and execute the query
	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# key order is [NxtHr] and [Nxtp1Hr]
	( undef, $Error_Status ) = &Query_Execute( $Sth, $Next_Hour_DateTime, $Next_Hour_p1_DateTime );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# populate %Reservation_Data_from_DB with the data fetched from the database
	my %Reservation_Data_from_DB;
	@Reservation_Data_from_DB{@Fields_to_Read} = ();
	$Sth->bind_columns( map { \$Reservation_Data_from_DB{$_} } @Fields_to_Read );

	my $ReservationID_Field_Name = shift( @Fields_to_Read );
	my %Reservations_to_Activate;

	while ( $Sth->fetch() )
	{
		######## QA POINT ########
		foreach $Field_Name ( @Fields_to_Read )
		{
			$Reservations_to_Activate{$Reservation_Data_from_DB{$ReservationID_Field_Name}} = $Reservation_Data_from_DB{$Field_Name};
		}
	}

	&Query_Finish( $Sth );

	### DB Query: retrieve the email address of each reservation requester (for later use)
	# prepare the query beforehand, and execute it within the foreach loop
	$Query = "SELECT $db_table_field_name{'users'}{'user_email_primary'} FROM $db_table_name{'users'} WHERE $db_table_field_name{'users'}{'user_loginname'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	foreach $Reservation_ID ( keys %Reservations_to_Activate )
	{
		# execute query
		( undef, $Error_Status ) = &Query_Execute( $Sth, $Reservations_to_Activate{$Reservation_ID}{'user_loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_CLI_Error_Screen( $script_filename, $Error_Status );
		}

		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			$Reservations_to_Activate{$Reservation_ID}{'user_email_primary'} = $$Ref[0];
		}
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

### for each reservation to activate,
### get ingress/egress edge router nodes
### and contact each node to build MPLS tunnels

### CAUTION: This code block is customized for Abilene. Further changes might be necessary to adapt the system to other types of network.

	foreach $Reservation_ID ( keys %Reservations_to_Activate )
	{
		# look up the closest Abilene node to each host (origin/destination)
		# if the node does not exist, the network path does not go past Abilene
		my %Closest_Node;

		# origin first; and then, destination (do not change the order of keys below!)
		foreach $Key ( 'origin', 'destination' )
		{
			my @TempNodeLookupResult;

			if ( $Key eq 'origin' )
			{
				# connect to the Indianapolis node (which is the closest to the login server)
				# and run traceroute to the origin to find out the closest Abilene node to the origin
				@TempNodeLookupResult = &Abilene_Node_Lookup( 'ipls', $Reservations_to_Activate{$Reservation_ID}{'reserv_origin_ip'} );
			}
			else
			{
				# connect to the Abilene node that is closest to the origin
				# and run traceroute from that node to the destination host
				@TempNodeLookupResult = &Abilene_Node_Lookup( $Closest_Node{'origin'}, $Reservations_to_Activate{$Reservation_ID}{'reserv_dest_ip'} );
			}

			if ( $TempNodeLookupResult[0] != 1 )
			{
				if ( $TempNodeLookupResult[0] eq 'command_fail' )
				{
					my $Error_Message = "An error has occurred while traversing the network path.\nReservation ID: $Reservation_ID\nUser: $Reservations_to_Activate{$Reservation_ID}{'user_loginname'}";
					&Email_Error_to_Admin( $Error_Message );
					&Print_CLI_Error_Screen( $script_filename, '', $Error_Message );
				}
				elsif ( $TempNodeLookupResult[0] eq 'non_abilene' )	# this error should never happen, though (it should have been caught at the time of reservation)
				{
					my $Error_Message = "This origin-destination path does not go through the Abilene network. Please check the origin and destination IP addresses.\nReservation ID: $Reservation_ID\nUser: $Reservations_to_Activate{$Reservation_ID}{'user_loginname'}";
					&Email_Error_to_Admin( $Error_Message );
					&Print_CLI_Error_Screen( $script_filename, '', $Error_Message );
				}
			}
			else
			{
				$Closest_Node{$Key} = $TempNodeLookupResult[1];
			}
		}

		# for later use (to insert into the "active_reservations" table)
		$Reservations_to_Activate{$Reservation_ID}{'active_tunnel_ingress'} = $Closest_Node{'origin'};
		$Reservations_to_Activate{$Reservation_ID}{'active_tunnel_egress'} = $Closest_Node{'destination'};

		# get IP addresses of each node
		my( $Ingress_Router_IPaddr, $Egress_Router_IPaddr ) = ( $abilene_nms2_nodes_addr{$Closest_Node{'origin'}}, $abilene_nms2_nodes_addr{$Closest_Node{'destination'}} );

		# build MPLS tunnels (bi-directional)
		# send: $OpMode [build/remove], $Reservation_ID, $Ingress_Router_Name, $Egress_Router_Name, $Ingress_Router_IPaddr, $Egress_Router_IPaddr, $MPLS_LSP_Bandwidth
		my $TunnelBuildResult = &Abilene_MPLS_Tunnel_Setup( 'build', $Reservation_ID, @Closest_Node{'origin', 'destination'}, $Ingress_Router_IPaddr, $Egress_Router_IPaddr, $Reservations_to_Activate{$Reservation_ID}{'reserv_bandwidth'} );

		# when succeeded, add a new key to the %Reservations_to_Activate hash for later use
		if ( $TunnelBuildResult == 1 )
		{
			$Reservations_to_Activate{$Reservation_ID}{'tunnel_build_succeeded'} = 1;
			$Reservations_to_Activate{$Reservation_ID}{'active_tunnel_createdtime'} = &Create_Time_String( 'dbinput', time );
		}
		else
		{
			$Reservations_to_Activate{$Reservation_ID}{'tunnel_build_succeeded'} = 0;
			$Reservations_to_Activate{$Reservation_ID}{'tunnel_build_error_message'} = $TunnelBuildResult;
		}

	}

### when succeeded, insert data into the "active_reservations" table
### and email reservation requester and notify the tunnel activation

### when failed, email both the system admin and the reservation requester 
### and let them know of the problem.

	### start working with the database (again)
	undef $Error_Status;

	# connect to the database
	# lock is probably unnecessary because the active_reservations table is not written by any other process at the same time
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# DB Query: insert into database query statement
	# prepare the query beforehand, and execute it within the foreach loop
	$Query = "INSERT INTO $db_table_name{'active_reservations'} VALUES ( ?, ?, ?, ?, ?, ? )";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	### for each reservation...
	foreach $Reservation_ID ( keys %Reservations_to_Activate )
	{
		# check the new key of the %Reservations_to_Activate hash
		if ( $Reservations_to_Activate{$Reservation_ID}{'tunnel_build_succeeded'} )
		{
			# reservation_id, user_loginname, active_tunnel_ingress, active_tunnel_egress, active_tunnel_createdtime, reserv_end_time
			my @Stuffs_to_Insert = ( $Reservation_ID, @Reservations_to_Activate{ 'user_loginname', 'active_tunnel_ingress', 'active_tunnel_egress', 'active_tunnel_createdtime', 'reserv_end_time' } );

			( undef, $Error_Status ) = &Query_Execute( $Sth, @Stuffs_to_Insert );
			if ( $Error_Status != 1 )
			{
				&Database_Disconnect( $Dbh );
				&Print_CLI_Error_Screen( $script_filename, $Error_Status );
			}

			### email reservation requester and notify the tunnel activation
			open( MAIL, "|$cfg_sendmail{'binary_path_and_flags'} $Reservations_to_Activate{$Reservation_ID}{'user_email_primary'}" ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

				print MAIL 'From: ', $cfg_sendmail{'system_admin_email_address'}, "\n";
				print MAIL 'To: ', $Reservations_to_Activate{$Reservation_ID}{'user_email_primary'}, "\n";

				print MAIL 'Subject: BRUW tunnels have been built', "\n";
				print MAIL 'Content-Type: text/plain; charset="', $cfg_sendmail{'email_text_encoding'}, '"', "\n\n";
				
				print MAIL 'Your BRUW reservation number #', $Reservation_ID, 'has become activated, and network tunnels have been built between ', $Reservations_to_Activate{$Reservation_ID}{'reserv_origin_ip'}, ' and ', $Reservations_to_Activate{$Reservation_ID}{'reserv_dest_ip'}, '.', "\n";
				print MAIL 'The tunnels will be removed after ', $Reservations_to_Activate{$Reservation_ID}{'reserv_end_time'}, ' GMT.', "\n\n";
				print MAIL 'For more information, please visit the BRUW service Web site.', "\n\n";

				print MAIL '---------------------------------------------------', "\n";
				print MAIL '=== This is an auto-generated e-mail ===', "\n";

			close( MAIL ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

		}
		else
		{
			### email both the system admin and the reservation requester, and let them know of the problem
			foreach $Email_To_Addr ( $cfg_sendmail{'system_admin_email_address'}, $Reservations_to_Activate{$Reservation_ID}{'user_email_primary'} )
			{
				open( MAIL, "|$cfg_sendmail{'binary_path_and_flags'} $Email_To_Addr" ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

					print MAIL 'From: ', $cfg_sendmail{'system_admin_email_address'}, "\n";
					print MAIL 'To: ', $Email_To_Addr, "\n";

					print MAIL 'Subject: BRUW tunnel building error', "\n";
					print MAIL 'Content-Type: text/plain; charset="', $cfg_sendmail{'email_text_encoding'}, '"', "\n\n";
					
					print MAIL 'A problem has occurred while activating your BRUW reservation number #', $Reservation_ID, '.', "\n";
					print MAIL '[ERROR] ', $Reservations_to_Activate{$Reservation_ID}{'tunnel_build_error_message'}, "\n\n";

					print MAIL '---------------------------------------------------', "\n";
					print MAIL '=== This is an auto-generated e-mail ===', "\n";

				close( MAIL ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );
			}
		}
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

exit;

##### End of mainstream #####

##### Beginning of sub routines #####

##### sub Email_Error_to_Admin
# In: $Error_Message
# Out: None
sub Email_Error_to_Admin
{

	open( MAIL, "|$cfg_sendmail{'binary_path_and_flags'} $cfg_sendmail{'system_admin_email_address'}" ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

		print MAIL 'From: ', $cfg_sendmail{'system_admin_email_address'}, "\n";
		print MAIL 'To: ', $cfg_sendmail{'system_admin_email_address'}, "\n";

		print MAIL 'Subject: BRUW system operation error at ',  $script_filename, "\n";
		print MAIL 'Content-Type: text/plain; charset="', $cfg_sendmail{'email_text_encoding'}, '"', "\n\n";
		
		print MAIL 'BRUW system error has occurred at ', $script_filename, "\n\n";

		print MAIL $_[0], "\n\n";

		print MAIL '---------------------------------------------------', "\n";
		print MAIL '=== This is an auto-generated e-mail ===', "\n";

	close( MAIL ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

}
##### End of sub Email_Error_to_Admin

##### End of sub routines #####

##### End of script #####
