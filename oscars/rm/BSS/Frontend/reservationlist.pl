#!/usr/bin/perl

# reservationlist.pl:  Reservation List DB handling
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of sub routines #####

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

##### End of sub routines #####

##### End of script #####
