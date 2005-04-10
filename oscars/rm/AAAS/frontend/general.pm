package AAAS::Frontend::General;

# general.pm:  package for general AAAS db usage
# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### List of sub routines #####

### format/obtain time string 
# sub Create_Time_String
# sub Create_Time_Sprintf_String
# sub Get_Offset_Adjusted_Time
# sub Calc_Local_Timezone_Offset
# sub Format_Offset_from_Seconds

##### List of sub routines End #####

##### Start of subroutines

##### sub Create_Time_String
# In: $String_Type [dbinput/(localtime)], $Time_Value, $String_Format
# Out: $Formatted_Time_String
### Look at sub Create_Time_Sprintf_String for $String_Format rules
# $String_Format is used only when $String_Type is not specified (empty)
sub Create_Time_String
{

	my $String_Type = $_[0];
	my $Time_Value = $_[1];
	my $String_Format = $_[2];

	# if $Time_Value is not specified, use the current time
	unless( defined( $Time_Value ) )
	{
		$Time_Value = time;
	}

	my( %Time, @Days, $Formatted_Time_String );

	if ( $String_Type eq 'dbinput' )
	{
		# get the GMT value of the specified time
		@Time{ 'sec', 'min', 'hour', 'mday', 'mon', 'year', 'wday', 'yday', 'isdst' } = gmtime( $Time_Value );
		@Days = ( 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat' );
		$Time{'month'} = $Time{'mon'} + 1;
	}
	else
	{
		@Time{ 'sec', 'min', 'hour', 'mday', 'mon', 'year', 'wday', 'yday', 'isdst' } = localtime( $Time_Value );
		@Days = ( 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat' );
		$Time{'month'} = $Time{'mon'} + 1;
	}

	$Time{'thisday'} = $Days[$Time{'wday'}];
	$Time{'year'} += 1900 if length( $Time{'year'} ) < 4;
	$Time{'year'} += 100 if $Time{'year'} <= 1970;

	if ( $String_Type eq 'dbinput' )
	{
		$Formatted_Time_String = sprintf( "%04d-%02d-%02d %02d:%02d:%02d", @Time{ 'year', 'month', 'mday', 'hour', 'min', 'sec' } );
	}
	else
	{
		my( $Sprintf_String, @Time_Hash_Keys ) = &Create_Time_Sprintf_String( $String_Format );
		$Formatted_Time_String = sprintf( "$Sprintf_String", @Time{ @Time_Hash_Keys } );
	}

	return $Formatted_Time_String;

} 
##### End of sub Create_Time_String


##### sub Create_Time_Sprintf_String
# In: $String_Format
# Out: $Sprintf_String (string to feed to sprintf), @Time_Hash_Keys (list of %Time hash keys; used in sub Create_Time_String to sprintf)
### $String_Format follows the following rule:
# year: {yyyy}, month(force 2 digits): {mm}, month(1-2 digit): {M}
# date(force 2 digits): {dd}, date(1-2 digit): {D}, day of week: {day}
# hour: {hh}, minute: {min}, second: {sec}
# Ex: {yyyy}/{mm}/{dd} ({day}) {hh}:{min}:{sec} = 2001/03/05 (Mon) 04:08:14
sub Create_Time_Sprintf_String
{

	my $Date_Stamp = $_[0];
	my( $Date_Stamp_Copy1, $Date_Stamp_Copy2, @Lists_of_Date_Stamp );

	$Date_Stamp_Copy1 = $Date_Stamp;

	foreach ( $Date_Stamp_Copy1 )
	{
		s/\{yyyy\}/%04d/g;
		s/\{mm\}/%02d/g;
		s/\{M\}/%d/g;
		s/\{dd\}/%02d/g;
		s/\{D\}/%d/g;
		s/\{day\}/%s/g;
		s/\{hh\}/%02d/g;
		s/\{min\}/%02d/g;
		s/\{sec\}/%02d/g;
	}

	$Date_Stamp_Copy2 = $Date_Stamp;

	foreach ( $Date_Stamp_Copy2 )
	{
		s/\{yyyy\}/{{year}}/g;
		s/\{mm\}/{{month}}/g;
		s/\{M\}/{{month}}/g;
		s/\{day\}/{{thisday}}/g;
		s/\{dd\}/{{mday}}/g;
		s/\{D\}/{{mday}}/g;
		s/\{hh\}/{{hour}}/g;
		s/\{min\}/{{min}}/g;
		s/\{sec\}/{{sec}}/g;
	}

	while ( $Date_Stamp_Copy2 =~ /\{\{(\w+)\}\}/g ) 
	{ 
		push( @Lists_of_Date_Stamp, $1 );
	}

	return $Date_Stamp_Copy1, @Lists_of_Date_Stamp;

}
##### End of sub Create_Time_Sprintf_String


##### sub Get_Offset_Adjusted_Time
# In: $Timezone_Offset (format: [+/-]hhmm (ex: +0930, -0400)), $Time_Value
# Out: $Time_w_Offset (adjusted "time" value)
# [Caution] The value of $Timezone_Offset is not checked for its validity
sub Get_Offset_Adjusted_Time
{

	my( $UTC_Offset_Sign, $UTC_Offset_Hour, $UTC_Offset_Minute ) = unpack( "aa2a2", $_[0] );

	# if $Time_Value is not specified, use the current time
	my $Time_Value = $_[1];

	unless( defined( $Time_Value ) )
	{
		$Time_Value = time;
	}

	# make the hour/minutes values numeric (09 -> 9)
	$UTC_Offset_Hour += 0;
	$UTC_Offset_Minute += 0;

	my $UTC_Offset_in_Seconds = ( $UTC_Offset_Hour * 60 * 60 ) + ( $UTC_Offset_Minute * 60 );

	if ( $UTC_Offset_Sign eq '-' )
	{
		$UTC_Offset_in_Seconds = $UTC_Offset_in_Seconds * -1;
	}

	# "time" is the current server time (in seconds); it has its own local utc offset built-in
	my $Time_w_Offset = $Time_Value + &Calc_Local_Timezone_Offset( 'seconds' ) + $UTC_Offset_in_Seconds;

	return $Time_w_Offset;

} 
##### End of sub Get_Offset_Adjusted_Time


##### sub Calc_Local_Timezone_Offset
# In: $Format [seconds/hhmm]
# Out: $Local_UTC_Offset (The UTC offset (in HHMM) of the current machine)
sub Calc_Local_Timezone_Offset
{

	use Time::Local;
	my $Return_Format = $_[0];

	my @Time = gmtime( time );
	my $Difference_in_Seconds = timegm( @Time ) - timelocal( @Time );

	if ( $Return_Format eq 'hhmm' )
	{
		return &Format_Offset_from_Seconds($Difference_in_Seconds);
	}
	else
	{
		return $Difference_in_Seconds;
	}

}
##### End of sub Calc_Local_Timezone_Offset


##### sub Format_Offset_from_Seconds
# In: $Offset_in_Seconds
# Out: $Offset_in_HHMM
# Changes -14400 (seconds) to -0400 (hours, minutes)
sub Format_Offset_from_Seconds
{

	my $Offset_in_Seconds = $_[0] + 0;	# +0 to make it numeric
	my $Offset_in_HHMM;

	if ( $Offset_in_Seconds != 0 )
	{
		my( $Sign, $Secs );

		if ( $Offset_in_Seconds < 0 )
		{
			$Sign = '-';
			$Secs = $Offset_in_Seconds * -1;
		}
		else
		{
			$Sign = '+';
			$Secs = $Offset_in_Seconds;
		}

		# NOTE: the following code will return "+0000" if you give it a number
		# of seconds that are a multiple of a day.
		my $Hours = $Secs / ( 60 * 60 );
		$Hours = $Hours % 24;
		my $Mins = ( $Secs % ( 60 * 60 ) ) / 60;

		$Offset_in_HHMM = sprintf( '%s%02d%02d', $Sign, $Hours, $Mins );
	}

	return $Offset_in_HHMM;

}
##### End of sub Format_Offset_from_Seconds


# Don't touch the line below
1;
