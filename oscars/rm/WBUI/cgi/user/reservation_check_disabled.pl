#!/usr/bin/perl

# reservation_check_disabled.pl:  Main interface CGI program for network
#                                 resource reservation process
# Last modified: April 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/authenticate.pl';

# current script name
$script_filename = $ENV{'SCRIPT_NAME'};

##### Beginning of mainstream #####

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

# login URI
$login_URI = 'https://oscars.es.net/';

if (!(Verify_Login_Status('', undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}


my $Error_Status = &Process_Reservation();
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


##### sub Process_Reservation
# In: None
# Out: None
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
			return( 0, '[ERROR] Please provide an IPv4 IP address for the ' . $_ . ' location.' );
		}
	}

	if ( $FormData{'origin'} eq $FormData{'destination'} )
	{
		return( 0, '[ERROR] Please provide different IP addresses for origin and destination locations.' );
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
        ## TODO:  connect to BSS and get back results

	return( 1, 'Your reservation has been processed successfully. Your reservation ID number is ' . $New_Reservation_ID . '.' );

}
##### End of sub Process_Reservation


##### End of sub routines #####

##### End of script #####
