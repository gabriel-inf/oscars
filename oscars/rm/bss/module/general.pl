#

# general.pl
#
# library for general cgi script usage
# Last modified: April 01, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

##### Settings Begin (Global variables) #####
# contact info
$webmaster = 'dwrobertson@lbl.gov';

##### Settings End #####


##### List of sub routines #####
### parse CGI form input
# sub Parse_Form_Input_Data

### format/obtain time string 
# sub Create_Time_String
# sub Create_Time_Sprintf_String
# sub Get_Offset_Adjusted_Time
# sub Calc_Local_Timezone_Offset
# sub Format_Offset_from_Seconds

### password encryption & random string generation (activation key, password reset (future), ...)
# sub Encode_Passwd
# sub Generate_Random_String

### print error screen to the browser
# sub Print_Error_Screen
# sub Print_CLI_Error_Screen
# sub Error_Code_To_Error_Message

##### List of sub routines End #####


##### sub Parse_Form_Input_Data
# In: $Form_Method [all/post/get]
# Out: %Form_Data (name-value pairs)
sub Parse_Form_Input_Data
{

	my $Form_Method = $_[0];
	
	my( $Form_Info, @Key_Value_Pairs, $Key, $Value, %Form_Data );

	if ( $Form_Method eq 'all' )
	{
		if ( $ENV{'REQUEST_METHOD'} =~ /^POST$/i )
		{
			read( STDIN, $Form_Info, $ENV{'CONTENT_LENGTH'} );
		}
		elsif ( $ENV{'REQUEST_METHOD'} =~ /^GET$/i )
		{
			$Form_Info = $ENV{'QUERY_STRING'}; 
		}
	}
	elsif ( $Form_Method eq 'post' )
	{
		read( STDIN, $Form_Info, $ENV{'CONTENT_LENGTH'} );
	}
	elsif ( $Form_Method eq 'get' )
	{
		$Form_Info = $ENV{'QUERY_STRING'}; 
	}

	@Key_Value_Pairs = split( /&/, $Form_Info );

	foreach $_ ( @Key_Value_Pairs )
	{
		( $Key, $Value ) = split( /=/, $_ );
		$Value =~ tr/+/ /;
		$Value =~ s/%([\dA-Fa-f][\dA-Fa-f])/pack( "C", hex( $1 ) )/eg;

		foreach ( $Value )
		{
#			s/\r|\n//g;	# removes line feeds
			s/^\s+|\s+$//g;	# removes leading/trailing spaces
			s/&/&amp;/g;
			s/&amp;\#([0-9]+);/&\#$1;/g;	# takes care of non-latin characters
			s/</&lt;/g;
			s/>/&gt;/g;
			s/"/&quot;/g;
		}

		$Form_Data{$Key} = $Value;
	}

	return %Form_Data;

} 
##### End of sub Parse_Form_Input_Data


##### sub Create_Time_String
# In: $String_Type [cookie/dbinput/(localtime)], $Time_Value, $String_Format
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

	if ( $String_Type eq 'cookie' )
	{
		@Time{ 'sec', 'min', 'hour', 'mday', 'mon', 'year', 'wday', 'yday', 'isdst' } = gmtime( $Time_Value );
		@Days = ( 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday' );
		$Time{'month'} = ( 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' )[$Time{'mon'}];
	}
	elsif ( $String_Type eq 'dbinput' )
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

	if ( $String_Type eq 'cookie' )
	{
		# for cookie spec, see here: http://wp.netscape.com/newsref/std/cookie_spec.html
		$Formatted_Time_String = sprintf( "%s, %02d-%s-%04d %02d:%02d:%02d GMT", @Time{ 'thisday', 'mday', 'month', 'year', 'hour', 'min', 'sec' } );
	}
	elsif ( $String_Type eq 'dbinput' )
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


##### sub Encode_Passwd
# In: $Raw_Password (plain text)
# Out: $Crypted_Password
sub Encode_Passwd
{

	my $Raw_Pwd = $_[0];
	my( $Crypted_Pwd, $Salt );
 
	$Salt = substr( $Raw_Pwd, -2, 2 );
	$Crypted_Pwd = crypt( $Raw_Pwd, $Salt );
 
	return $Crypted_Pwd;

}
##### End of sub Encode_Passwd


##### sub Generate_Random_String
# In: $String_Length
# Out: $Random_String
# This sub routine takes care of generating a random string for all functions except cookie randomkey
sub Generate_Random_String
{

	my $String_Length = $_[0] + 0;	# make it a numeric value

	my @Alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
	my $Random_String = join( '', map( $Alphanumeric[ rand @Alphanumeric ], 0 .. $String_Length ) );

	return $Random_String;

}
##### End of sub Generate_Random_String


##### sub Print_Error_Screen
# In: $Err_Location, "$Err_Code (to be referenced by &Error_Code_To_Error_Message)\n$Errno (optional; $! in usual)"
# Out: None (prints error screen to the browser and exits)
sub Print_Error_Screen
{

#	my $Err_Location = $ENV{'SCRIPT_NAME'};

	my $Err_Location = $_[0];
	my( $Err_Code, $Errno ) = split( /\n/, $_[1], 2 );

	my $Err_Message = &Error_Code_To_Error_Message( $Err_Code );

	print <<__HTML__;
Pragma: no-cache
Cache-control: no-cache
Content-type: text/html

<html>
	<head>
		<title>500 CGI internal Error</title>
	</head>
	<body>
	<h1>500 CGI internal Error</h1>

	<p>An internal error occurred at <em>$Err_Location</em>.</p>
	<p>Please contact the webmaster at $webmaster,
	and inform the person of the date and time of error and
	anything you might have done that may have caused the problem.</p>
__HTML__

	if ( $Errno ne '' )
	{
		print "<p><strong>[Error] $Err_Message<br>$Errno</strong></p>\n";
	}
	else
	{
		print "<p><strong>[Error] $Err_Message</strong></p>\n";
	}

	print "</body></html>\n";

	exit;

}
##### End of sub Print_Error_Screen


##### sub Print_CLI_Error_Screen
# In: $Err_Location, "$Err_Code (to be referenced by &Error_Code_To_Error_Message)\n$Errno (optional; $! in usual)", $Custom_Err_Message (optional)
# Out: None (prints error screen to standard output and exits)
# $Custom_Err_Message is checked only when $_[1] is empty
# lock release should be taken care of before calling this sub routine
sub Print_CLI_Error_Screen
{

#	my $Err_Location = $ENV{'SCRIPT_NAME'};

	my $Err_Location = $_[0];
	my( $Err_Code, $Errno, $Err_Message );

	if ( $_[1] ne '' )
	{
		( $Err_Code, $Errno ) = split( /\n/, $_[1], 2 );
		$Err_Message = &Error_Code_To_Error_Message( $Err_Code );
	}
	else
	{
		$Err_Message = $_[2];
	}

	if ( $Errno ne '' )
	{
		die "An error has occurred at ${Err_Location}: $Err_Message\n$Errno";
	}
	else
	{
		die "An error has occurred at ${Err_Location}: $Err_Message";
	}

	exit;

}
##### End of sub Print_CLI_Error_Screen


##### sub Error_Code_To_Error_Message
# In: $Error_Code
# Out: $Err_Message
sub Error_Code_To_Error_Message
{

#	my $errmsglang = 'en';

	my %Error_Msg_Table_EN = (
		FileOpen => "Cannot open file. Please check if the file location is right, the file permission is properly set, or the file is not corrupted.",
		CantConnectDB => "Cannot connect to the database.",
		CantPrepareStatement => "Cannot prepare database statement.",
		CantExecuteQuery => "Cannot execute query in the database.",
		CantOpenSendmail => "Cannot run Sendmail.",
	);

#	my %Error_Msg_Set = (
#			en => { %Error_Msg_Table_EN }
#	);

#	return $Error_Msg_Set{$errmsglang}{$_[0]};

	return $Error_Msg_Table_EN{$_[0]};

} 
##### End of sub Error_Code_To_Error_Message


##### End of Library File
# Don't touch the line below
1;
