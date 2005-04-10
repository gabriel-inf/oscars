#!/usr/bin/perl

# bruw_remove_tunnels.pl
#
# Back-end scheduler: Go through the reservation database and remove expiring MPLS tunnels
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
$script_filename = 'bruw_removetunnels.cgi';

# set AUTOFLUSH to true
$| = 1;

##### Beginning of mainstream #####

### task order: delete expiring tunnels -> move all past reservations to a different db table
### this script is supposed to run about 3-5 minutes after every hour

### 1. Remove expiring tunnels from both edge routers

### read 'active_reservations' table from the database
### and get the list of tunnels to delete

	### for date/time calculation
	use DateTime;

	# get the current hour
	my $dtCurHr = DateTime->now()->truncate( to => 'hour' );

	# 'dbinput' type uses gmtime( epoch_time ); hence convert the time to an offset from the epoch using the $dt->epoch() method
	my $Current_Hour_DateTime = &Create_Time_String( 'dbinput', $dtCurHr->epoch() );

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
	my @Fields_to_Read = ( 'reservation_id', 'user_loginname', 'active_tunnel_name', 'active_tunnel_ingress', 'active_tunnel_egress', 'reserv_end_time' );

	### DB Query: get the information of expiring reservations
	# condition: Res.EndingTime <= [CurHr]
	$Query = "SELECT ";
	foreach $_ ( @Fields_to_Read )
	{
		$Query .= $db_table_field_name{'active_reservations'}{$_} . ", ";
	}
	# delete the last ", "
	$Query =~ s/,\s$//;
	# datetime conditions
	$Query .= " FROM $db_table_name{'active_reservations'} WHERE $db_table_field_name{'active_reservations'}{'reserv_end_time'} <= ?";

	# prepare and execute the query
	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $Current_Hour_DateTime );
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
	my %Reservations_to_Deactivate;

	while ( $Sth->fetch() )
	{
		######## QA POINT ########
		foreach $Field_Name ( @Fields_to_Read )
		{
			$Reservations_to_Deactivate{$Reservation_Data_from_DB{$ReservationID_Field_Name}} = $Reservation_Data_from_DB{$Field_Name};
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

	foreach $Reservation_ID ( keys %Reservations_to_Deactivate )
	{
		# execute query
		( undef, $Error_Status ) = &Query_Execute( $Sth, $Reservations_to_Deactivate{$Reservation_ID}{'user_loginname'} );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_CLI_Error_Screen( $script_filename, $Error_Status );
		}

		while ( my $Ref = $Sth->fetchrow_arrayref )
		{
			$Reservations_to_Deactivate{$Reservation_ID}{'user_email_primary'} = $$Ref[0];
		}
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

### for each reservation to deactivate/remove,
### contact both ingress and egress nodes to delete MPLS tunnels

### CAUTION: This code block is customized for Abilene. Further changes might be necessary to adapt the system to other types of network.

	foreach $Reservation_ID ( keys %Reservations_to_Deactivate )
	{
		# get IP addresses of each node
		my( $Ingress_Router_IPaddr, $Egress_Router_IPaddr, $Ingress_Router_Name, $Egress_Router_Name ) = ( $abilene_nms2_nodes_addr{$Reservations_to_Deactivate{$Reservation_ID}{'active_tunnel_ingress'}}, $abilene_nms2_nodes_addr{$Reservations_to_Deactivate{$Reservation_ID}{'active_tunnel_egress'}}, $Reservations_to_Deactivate{$Reservation_ID}{'active_tunnel_ingress'}, $Reservations_to_Deactivate{$Reservation_ID}{'active_tunnel_egress'} );

		# build MPLS tunnels (bi-directional)
		# send: $OpMode [build/remove], $Reservation_ID, $Ingress_Router_Name, $Egress_Router_Name, $Ingress_Router_IPaddr, $Egress_Router_IPaddr, $MPLS_LSP_Bandwidth
		# bandwidth information is not necessary when tearing down a tunnel (the only necessary information is just the tunnel name, actually, which is constructed from the reservation ID and both node names)
		my $TunnelRemoveResult = &Abilene_MPLS_Tunnel_Setup( 'remove', $Reservation_ID, $Ingress_Router_Name, $Egress_Router_Name, $Ingress_Router_IPaddr, $Egress_Router_IPaddr, '' );

		# when succeeded, add a new key to the %Reservations_to_Deactivate hash for later use
		if ( $TunnelRemoveResult == 1 )
		{
			$Reservations_to_Deactivate{$Reservation_ID}{'tunnel_remove_succeeded'} = 1;
		}
		else
		{
			$Reservations_to_Deactivate{$Reservation_ID}{'tunnel_remove_succeeded'} = 0;
			$Reservations_to_Deactivate{$Reservation_ID}{'tunnel_remove_error_message'} = $TunnelRemoveResult;
		}
	}

### when succeeded, remove data from the 'active_reservations' table
### and email reservation requester and notify the tunnel deactivation

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

	# DB Query: delete removed tunnel information from the 'active_reservations' table
	# prepare the query beforehand, and execute it within the foreach loop
	$Query = "DELETE FROM $db_table_name{'active_reservations'} WHERE $db_table_field_name{'active_reservations'}{'reservation_id'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# for each reservation...
	foreach $Reservation_ID ( keys %Reservations_to_Deactivate )
	{
		# check the new key of the %Reservations_to_Deactivate hash
		if ( $Reservations_to_Deactivate{$Reservation_ID}{'tunnel_remove_succeeded'} )
		{

			( undef, $Error_Status ) = &Query_Execute( $Sth, $Reservation_ID );
			if ( $Error_Status != 1 )
			{
				&Database_Disconnect( $Dbh );
				&Print_CLI_Error_Screen( $script_filename, $Error_Status );
			}

			### email reservation requester and notify the tunnel removal
			open( MAIL, "|$cfg_sendmail{'binary_path_and_flags'} $Reservations_to_Deactivate{$Reservation_ID}{'user_email_primary'}" ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

				print MAIL 'From: ', $cfg_sendmail{'system_admin_email_address'}, "\n";
				print MAIL 'To: ', $Reservations_to_Deactivate{$Reservation_ID}{'user_email_primary'}, "\n";

				print MAIL 'Subject: BRUW tunnels have been removed', "\n";
				print MAIL 'Content-Type: text/plain; charset="', $cfg_sendmail{'email_text_encoding'}, '"', "\n\n";
				
				print MAIL 'Your BRUW reservation number #', $Reservation_ID, 'has become deactivated, and all associated network tunnels have been removed.', "\n";
				print MAIL 'For more information, please visit the BRUW service Web site.', "\n\n";

				print MAIL '---------------------------------------------------', "\n";
				print MAIL '=== This is an auto-generated e-mail ===', "\n";

			close( MAIL ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

		}
		else
		{
			### email both the system admin and the reservation requester, and let them know of the problem
			foreach $Email_To_Addr ( $cfg_sendmail{'system_admin_email_address'}, $Reservations_to_Deactivate{$Reservation_ID}{'user_email_primary'} )
			{
				open( MAIL, "|$cfg_sendmail{'binary_path_and_flags'} $Email_To_Addr" ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );

					print MAIL 'From: ', $cfg_sendmail{'system_admin_email_address'}, "\n";
					print MAIL 'To: ', $Email_To_Addr, "\n";

					print MAIL 'Subject: BRUW tunnel removal error', "\n";
					print MAIL 'Content-Type: text/plain; charset="', $cfg_sendmail{'email_text_encoding'}, '"', "\n\n";
					
					print MAIL 'A problem has occurred while deactivating your BRUW reservation number #', $Reservation_ID, '.', "\n";
					print MAIL '[ERROR] ', $Reservations_to_Deactivate{$Reservation_ID}{'tunnel_remove_error_message'}, "\n\n";

					print MAIL '---------------------------------------------------', "\n";
					print MAIL '=== This is an auto-generated e-mail ===', "\n";

				close( MAIL ) || &Print_CLI_Error_Screen( $script_filename, "CantOpenSendmail\n" . $! );
			}
		}
	}

	&Query_Finish( $Sth );

	# don't disconnect from the database yet!

### 2. Move all past reservations to the 'past_reservations' table
### Note: Do not move a reservation if it's still in the active reservations table

### read 'reservations' table from the database
### get the list of reservation data to move

	undef $Error_Status;
	my( @Past_Reservations, @Past_Reservations_but_Active, @Past_Reservations_to_Move );

	### DB Query: get the reservation IDs of all past reservations
	# condition: Res.EndingTime <= [CurHr]
	$Query = "SELECT $db_table_field_name{'reservations'}{'reservation_id'} FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'reserv_end_time'} <= ?";

	# prepare and execute the query
	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# $Current_Hour_DateTime from above is reused here
	( undef, $Error_Status ) = &Query_Execute( $Sth, $Current_Hour_DateTime );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		push( @Past_Reservations, $$Ref[0] );
	}

	&Query_Finish( $Sth );

### read 'active_reservations' table from the database
### and compare reservation ID to see whether the reservation is still in active state

	### DB Query: get the reservation IDs of all past reservations from the active_reservations table
	# condition: Res.EndingTime <= [CurHr]
	$Query = "SELECT $db_table_field_name{'active_reservations'}{'reservation_id'} FROM $db_table_name{'active_reservations'} WHERE $db_table_field_name{'active_reservations'}{'reserv_end_time'} <= ?";

	# prepare and execute the query
	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	# $Current_Hour_DateTime from above is reused here, again
	( undef, $Error_Status ) = &Query_Execute( $Sth, $Current_Hour_DateTime );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	while ( my $Ref = $Sth->fetchrow_arrayref )
	{
		push( @Past_Reservations_but_Active, $$Ref[0] );
	}

	&Query_Finish( $Sth );

	# compare two past reservations list
	# and only collect the IDs of the reservations that are past but not in active state
	if ( $#Past_Reservations_but_Active >= 0 )
	{
		foreach $TempResID ( @Past_Reservations )
		{
			foreach $_ ( @Past_Reservations_but_Active )
			{
				if ( $TempResID ne $_ )
				{
					push( @Past_Reservations_to_Move, $TempResID );
				}
			}
		}
	}

### move all qualifying past reservation data from 'reservations' to 'past_reservations'
# Note: the two tables are identital in table field structure

	# DB Query: select all qualifying past reservation data from 'reservations' and insert them into 'past_reservations'
	# prepare the query beforehand, and execute it within the foreach loop
	$Query = "INSERT INTO $db_table_name{'past_reservations'} SELECT * FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'reservation_id'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	foreach $Reservation_ID ( @Past_Reservations_to_Move )
	{
		( undef, $Error_Status ) = &Query_Execute( $Sth, $Reservation_ID );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_CLI_Error_Screen( $script_filename, $Error_Status );
		}
	}

	&Query_Finish( $Sth );

	# DB Query: delete moved reservation data from 'reservations'
	# prepare the query beforehand, and execute it within the foreach loop
	$Query = "DELETE FROM $db_table_name{'reservations'} WHERE $db_table_field_name{'reservations'}{'reservation_id'} = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_CLI_Error_Screen( $script_filename, $Error_Status );
	}

	foreach $Reservation_ID ( @Past_Reservations_to_Move )
	{
		( undef, $Error_Status ) = &Query_Execute( $Sth, $Reservation_ID );
		if ( $Error_Status != 1 )
		{
			&Database_Disconnect( $Dbh );
			&Print_CLI_Error_Screen( $script_filename, $Error_Status );
		}
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

exit;

##### End of mainstream #####

##### Beginning of sub routines #####

##### End of sub routines #####

##### End of script #####
