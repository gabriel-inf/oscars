#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: April 4, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

# check if the user is logged in
if ( &Verify_Login_Status( ) != 1 )
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
}
##### End of sub Print_Reservations_List


##### sub Print_Reservation_Detail
# In: None
# Out: None (exits the program at the end)
sub Print_Reservation_Detail
{
	### TODO:  get the reservation detail from the BSS

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

	# TODO:  print to browser screen
	exit;

}
##### End of sub Print_Reservation_Detail

##### End of sub routines #####

##### End of script #####
