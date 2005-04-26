#!/usr/bin/perl -w

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: April 19, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use DateTime;
use Socket;
use Data::Dumper

# include libraries
require '../lib/general.pl';

use BSS::Client::SOAPClient;
use AAAS::Client::Auth;


# login URI
$login_URI = 'https://oscars.es.net/';
$auth = AAAS::Client::Auth->new();

# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

if (!($auth->verify_login_status(\%FormData, undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}

my ($Error_Status, %Results) = create_reservation(\%FormData);

if (!$Error_Status)
{
    Update_Frames("", "Reservation made");
}
else
{
    Update_Frames("", $Results{'error_msg'});
}
exit;



##### Beginning of sub routines #####


##### sub create_reservation
# In: None
# Out: None
sub create_reservation
{

  my( $FormData ) = @_;
  my( %params );
  my( %results);

  $params{'id'} =              'NULL';

      # in seconds since epoch
  $params{'start_time'} =     $FormData->{'start_time'};
      # start time + duration time in seconds
  $params{'end_time'} =       $FormData->{'start_time'} + $FormData->{'duration_hour'} * 3600;

  $params{'created_time'} =   '';   # filled in scheduler
  $params{'bandwidth'} =      $FormData->{'bandwidth'};
  $params{'class'} =          'test';
  $params{'burst_limit'} =    '100m';
  $params{'status'} =         'pending';

  $params{'ingress_interface_id'}= '';   # db lookup in scheduler
  $params{'egress_interface_id'}=  '';   # db lookup in scheduler

  $params{'src_ip'} =         $FormData->{'origin'};
  $params{'dst_ip'} =         $FormData->{'destination'};

      # TODO:  error checking
  if (not_an_ip($params{'src_ip'})) {
      $params{'src_ip'} = inet_ntoa(inet_aton($params{'src_ip'}));
  }

  if (not_an_ip($params{'dst_ip'})) {
      $params{'dst_ip'} = inet_ntoa(inet_aton($params{'dst_ip'}));
  }

  $params{'dn'} =             'oscars';

  $params{'ingress_port'} =   '';     # db lookup in schedule
  $params{'egress_port'} =    '';     # db lookup in scheduler

  $params{'dscp'} =           '';     # optional

  $params{'description'} =    $FormData->{'description'};
  return( soap_create_reservation(\%params) );

}


sub not_an_ip
{
  my($string) = @_;

  return($string !~ /^([\d]+)\.([\d]+)\.([\d]+)\.([\d]+)$/);
}

##### End of sub routines #####

##### End of script #####
