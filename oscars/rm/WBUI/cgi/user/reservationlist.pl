#!/usr/bin/perl

# reservationlist.pl:  Main service: Reservation List
# Last modified: April 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use DateTime;
use Socket;
use CGI;


use BSS::Client::SOAPClient;

require '../lib/general.pl';



    # names of the fields to be read
my @fields_to_read = ( 'user_dn', 'start_time', 'end_time', 'status', 'src_id', 'dst_id' );


my (%form_params, %results);


my $cgi = CGI->new();
my $error_status = check_login(0, $cgi);

if (!$error_status) {
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }
  ($error_status, %results) = soap_get_reservations(\%form_params, \@fields_to_read);
  if (!$error_status) {
      update_frames("main_frame", "", $results{'status_msg'});
      print_reservations(\%results);
  }
  else {
      update_frames("main_frame", "", $results{'error_msg'});
  }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;



##### sub print_reservations
# In: 
# Out:
sub print_reservations
{
  my ( $results) = @_;
  my ( $rowsref, $mapping, $row );

  $rowsref = $results->{'rows'};
  $mapping = $results->{'idtoip'};
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/user/reservationlist.js\"></script>\n";
  print "</head>\n\n";

  print "<body onload=\"stripe('reservationlist', '#fff', '#edf3fe');\">\n\n";

  print "<script language=\"javascript\">print_navigation_bar('reservationList');</script>\n\n";

  print "<div id=\"zebratable_ui\">\n\n";

  print "<p><em>View Active Reservations</em><br>\n";
  print "<p>Click on the Reservation Tag link to view detailed information about the reservation.\n";
  print "</p>\n\n";

  print "<table cellspacing=\"0\" width=\"90%\" id=\"reservationlist\">\n";
  print "  <thead>\n";
  print "  <tr>\n";
  print "    <td >Tag</td>\n";
  print "    <td >Start Time</td>\n";
  print "    <td >End Time</td>\n";
  print "    <td >Status</td>\n";
  print "    <td >Origin</td>\n";
  print "    <td >Destination</td>\n";
  print "  </tr>\n";
  print "  </thead>\n";

  print "  <tbody>\n";
  foreach $row (@$rowsref)
  {
      if ($row{'status'} ne 'finished')
      {
        print "  <tr>\n";
        print_row($row, $mapping);
        print "  </tr>\n";
      }
  }
  print "  </tbody>\n";

  print "</table>\n\n";

  print "<p>For inquiries, please contact the project administrator.</p>\n\n";

  print "</div>\n\n";

  print "<script language=\"javascript\">print_footer();</script>\n";
  print "</body>\n";
  print "</html>\n\n";
}



sub print_row
{
  my( $row, $mapping ) = @_;
  my( $tag, $seconds, $time_field, $time_tag );


  $seconds = DateTime->from_epoch( epoch => $row{'start_time'} );
  ($time_tag, $time_field) = get_time_str($seconds);
      # ESnet hard wired for now in tag
      # TODO:  incremental ID at end if multiple ones in same minute
  $tag = 'OSCARS.ESnet.' . $row->{'user_dn'} . '.' . $time_tag . '.0';
  print "    <td>" . $tag . "</td>\n"; 
  
  print $time_field;

  $seconds = DateTime->from_epoch( epoch => $row{'end_time'} );
  ($time_tag, $time_field) = get_time_str($seconds);
  print $time_field;

  print "    <td>" . $row->{'status'} . "</td>\n";

  print_host($row{'src_id'}, $mapping);
  print_host($row{'dst_id'}, $mapping);
}


#

sub print_host
{
  my( $id, $mapping ) = @_;

  my $ip = $mapping->{'id'};
  my $ipaddr = inet_aton($ip);
  my $host = gethostbyaddr($ipaddr, AF_INET);
  if ($host) {
      print "    <td>" . $host . "</td>\n"; 
  }
  else
  {
      print "    <td>" . $ipaddr . "</td>\n"; 
  }
}


#

sub get_time_str 
{
  my( $seconds ) = @_;

  my $dt = DateTime->from_epoch( epoch => $seconds );
  my $year = $dt->year();
  if ($year < 10)
  {
      $year = "0" . $year;
  }
  my $month = $dt->month();
  if ($month < 10)
  {
      $month = "0" . $month;
  }
  my $day = $dt->day();
  if ($day < 10)
  {
      $day = "0" . $day;
  }
  my $hour = $dt->hour();
  if ($hour < 10)
  {
      $hour = "0" . $hour;
  }
  $minute = $dt->minute();
  if ($minute < 10)
  {
      $minute = "0" . $minute;
  }
  my $time_tag = $year . $month . $day . '.' . $hour . ':' . $minute;
  my $time_field = "      <td>" . $month . "-" . $day . "&nbsp;&nbsp; " . $hour . ":" . $minute . "</td>\n" ;

  return ( $time_tag, $time_field );
}
