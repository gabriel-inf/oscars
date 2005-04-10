#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: April 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept both POST and GET methods)
%FormData = &Parse_Form_Input_Data( 'all' );

$FormData{'loginname'} = ( &Read_Login_Cookie( $user_login_cookie_name ) )[1];

my $Error_Status = &Print_Reservation_Detail();
if ( !$Error_Status)
{
    &Print_Frames();
}
else
{
    &Print_Status_Message($Error_Status);
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


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
