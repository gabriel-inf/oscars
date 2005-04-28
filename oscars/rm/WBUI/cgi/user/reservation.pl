#!/usr/bin/perl -w

# reservation.pl:  Main interface CGI program for network resource
#                  reservation process
# Last modified: April 19, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use DateTime;
use Socket;
use Data::Dumper;
use CGI;

use BSS::Client::SOAPClient;

require '../lib/general.pl';


my $cgi = CGI->new();

my (%form_params, %results);

my $error_status = check_login(0, $cgi);

if (!$error_status) {
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }

  ($error_status, %results) = create_reservation(\%form_params);
  if (!$error_status) {
      update_frames("status_frame", "", $results{'status_msg'});
  }
  else {
      update_frames("status_frame", "", $results{'error_msg'});
  }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;


##### sub create_reservation
# In: None
# Out: None
sub create_reservation
{

  my( $form_params ) = @_;
  my( %soap_params );
  my( %results);

  $soap_params{'id'} =              'NULL';

      # in seconds since epoch
  $soap_params{'start_time'} =     $form_params->{'start_time'};
      # start time + duration time in seconds
  $soap_params{'end_time'} =       $form_params->{'start_time'} + $form_params->{'duration_hour'} * 3600;

  $soap_params{'created_time'} =   '';   # filled in scheduler
  $soap_params{'bandwidth'} =      $form_params->{'bandwidth'};
  $soap_params{'class'} =          'test';
  $soap_params{'burst_limit'} =    '100m';
  $soap_params{'status'} =         'pending';

  $soap_params{'ingress_interface_id'}= '';   # db lookup in scheduler
  $soap_params{'egress_interface_id'}=  '';   # db lookup in scheduler

  $soap_params{'src_ip'} =         $form_params->{'origin'};
  $soap_params{'dst_ip'} =         $form_params->{'destination'};

      # TODO:  error checking
  if (not_an_ip($soap_params{'src_ip'})) {
      $soap_params{'src_ip'} = inet_ntoa(inet_aton($soap_params{'src_ip'}));
  }

  if (not_an_ip($soap_params{'dst_ip'})) {
      $soap_params{'dst_ip'} = inet_ntoa(inet_aton($soap_params{'dst_ip'}));
  }

  $soap_params{'dn'} =             'oscars';

  $soap_params{'ingress_port'} =   '';     # db lookup in schedule
  $soap_params{'egress_port'} =    '';     # db lookup in scheduler

  $soap_params{'dscp'} =           '';     # optional

  $soap_params{'description'} =    $form_params->{'description'};
  return( soap_create_reservation(\%params) );

}


sub not_an_ip
{
  my($string) = @_;

  return($string !~ /^([\d]+)\.([\d]+)\.([\d]+)\.([\d]+)$/);
}
