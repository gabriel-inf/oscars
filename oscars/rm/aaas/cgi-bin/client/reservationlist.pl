#!/usr/bin/env perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require './lib/general.pl';
require './lib/database.pl';
require './lib/authenticate.pl';

# template html file names
%interface_template_filename = (
	'resvtable' => 'resvlist_table.html',
	'resvcalendar' => 'resvlist_calendar.html',
	'resvdetail' => 'resvdetail.html'
);

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

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

# if 'mode' eq 'resvdetail': Print reservation details
# all else (default): Print reservations list in table or in calendar
if ( $FormData{'mode'} eq 'resvdetail' )
{
	&Print_Reservation_Detail();
}
else
{
	&Print_Reservations_List();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####

##### sub Print_Reservations_List
# In: None
# Out: None (exits the program at the end)
sub Print_Reservations_List
{

	my $Template_HTML_File = $interface_template_filename{'resvtable'};

	# reserved functionality
	if ( $FormData{'view'} eq 'calendar' )
	{
		$Template_HTML_File = $interface_template_filename{'resvcalendar'};
	}

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	my $Html_Line = join( '', @Template_Html );

	### get the reservation list from the database and populate the table tag
	my( $Dbh, $Sth, $Error_Status, $Query );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
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

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
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

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach ( $Html_Line )
	{
		s/<!-- \(\(_Current_LoggedIn_Name_\)\) -->/$FormData{'loginname'}/g;
		s/<!-- \(\(_Reservation_List_Table_\)\) -->/$Reservation_List_Table/g;
	}

	print $Html_Line;

	exit;

}
##### End of sub Print_Reservations_List


##### sub Print_Reservation_Detail
# In: None
# Out: None (exits the program at the end)
sub Print_Reservation_Detail
{

	my $Template_HTML_File = $interface_template_filename{'resvdetail'};

	# open html template file
	open( F_HANDLE, $Template_HTML_File ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $Template_HTML_File . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	my $Html_Line = join( '', @Template_Html );

	### get the reservation detail from the database
	my( $Dbh, $Sth, $Error_Status, $Query );

	# connect to the database
	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
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

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( undef, $Error_Status ) = &Query_Execute( $Sth, $FormData{'resvid'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# populate %Reservations_Data with the data fetched from the database
	my %Reservations_Data;
	@Reservations_Data{@Fields_to_Display} = ();
	$Sth->bind_columns( map { \$Reservations_Data{$_} } @Fields_to_Display );
	$Sth->fetch();

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	# for date/time calculation
	use DateTime;

	# create duration string from start time & end time
	my( %TempStartDateTime, %TempEndDateTime );
	@TempStartDateTime{ 'year', 'month', 'day', 'hour', 'minute', 'second' } = ( unpack( "a4aa2aa2aa2aa2aa2", $Reservations_Data{'reserv_start_time'} ) )[0, 2, 4, 6, 8, 10];
	@TempEndDateTime{ 'year', 'month', 'day', 'hour', 'minute', 'second' } = ( unpack( "a4aa2aa2aa2aa2aa2", $Reservations_Data{'reserv_end_time'} ) )[0, 2, 4, 6, 8, 10];
	my $dtStart = DateTime->new( %TempStartDateTime, time_zone => 'UTC' );
	my $dtEnd = DateTime->new( %TempEndDateTime, time_zone => 'UTC' );
	my $durObj = $dtEnd - $dtStart;
	my @TempDuration = $durObj->in_units( 'days', 'hours' );
	my $Resv_Duration_String;
	$Resv_Duration_String .= $TempDuration[0] . " days "	if ( $TempDuration[0] > 1 );
	$Resv_Duration_String .= $TempDuration[0] . " day "	if ( $TempDuration[0] == 1 );
	$Resv_Duration_String .= $TempDuration[1] . " hours"	if ( $TempDuration[1] > 1 );
	$Resv_Duration_String .= $TempDuration[1] . " hour"	if ( $TempDuration[1] == 1 );

	# print to browser screen
	# Pragma: no-cache => Pre-HTTP/1.1 directive to prevent caching
	# Cache-control: no-cache => HTTP/1.1 directive to prevent caching
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach ( $Html_Line )
	{
		s/<!-- \(\(_Reservation_ID_Number_\)\) -->/$FormData{'resvid'}/g;
		s/<!-- \(\(_Reservation_Duration_\)\) -->/$Resv_Duration_String/g;
	}

	# substitution keywords in the HTML template; should align exactly with @Fields_to_Display
	my @Template_Keywords_to_Substitute = ( 'Requester_Login_Name', 'Origin_Location', 'Destination_Location', 'Requested_Bandwidth', 'Start_DateTime', 'End_DateTime', 'Reservation_Description', 'Request_DateTime' );

	foreach $_ ( 0 .. $#Fields_to_Display )
	{
		$Html_Line =~ s/<!-- \(\(_${Template_Keywords_to_Substitute[${_}]}_\)\) -->/$Reservations_Data{$Fields_to_Display[$_]}/g;
	}

	print $Html_Line;

	exit;

}
##### End of sub Print_Reservation_Detail

##### End of sub routines #####

##### End of script #####
