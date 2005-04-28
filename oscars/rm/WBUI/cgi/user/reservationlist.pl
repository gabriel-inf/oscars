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



    # names of the fields to be read and displayed on the screen
my @fields_to_read = ( 'start_time', 'end_time', 'bandwidth', 'status', 'src_id', 'dst_id' );


my (%form_params, %results);


my $cgi = CGI->new();
my $error_status = check_login(0, $cgi);

if (!$error_status) {
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }
      # FIX:  hard-wired for testing
  $form_params{'dn'} = 'oscars';
  ($error_status, %results) = soap_get_reservations(\%form_params, \@fields_to_read);
  if (!$error_status) {
      update_frames("main_frame", "", $results{'status_msg'});
      print_reservation_detail(\%results);
  }
  else {
      update_frames("main_frame", "", $results{'error_msg'});
  }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;



##### sub print_reservation_detail
# In: 
# Out:
sub print_reservation_detail
{
  my ($results) = @_;
  my ($arrayref, $row, $f, $fctr, $dt, $minute, $hour, $ipaddr, $host);

  $arrayref = $results->{'rows'};
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
  print "<p>Click the Reservation ID number link to view detailed information about the reservation.\n";
  print "(NOTE:  Currently this is mixed with the reservation details.)\n";
  print "</p>\n\n";

  print "<table cellspacing=\"0\" width=\"90%\" id=\"reservationlist\">\n";
  print "  <thead>\n";
  print "  <tr>\n";
  print "    <td width=\"13%\">Start Time</td>\n";
  print "    <td width=\"13%\">End Time</td>\n";
  print "    <td width=\"4%\">Bandwidth</td>\n";
  print "    <td width=\"12%\">Status</td>\n";
  print "    <td width=\"29%\">Origin</td>\n";
  print "    <td width=\"29%\">Destination</td>\n";
  print "  </tr>\n";
  print "  </thead>\n";

  print "  <tbody>\n";
      # ordering, number of fields has to be same on client and server
      # hash would be better, but haven't figured out how to read from db all
      # at once into hash
  foreach $row (@$arrayref)
  {
      if (@$row[3] ne 'finished')
      {
        print "  <tr>\n";
        $fctr = 0;
        foreach $f (@$row)
        {
            if ($fctr < 2)    # first two fields are starting and ending times
            {
              $dt = DateTime->from_epoch( epoch => $f );
              $hour = $dt->hour();
              if ($hour < 10)
              {
                  $hour = "0" . $hour;
              }
              $minute = $dt->minute();
              if ($minute < 10)
              {
                  $minute = "0" . $minute;
              }
              print "      <td>" . $dt->month() . "-" . $dt->day() . "&nbsp;&nbsp; " . $hour . ":" . $minute . "</td>\n" 
            }
            elsif (($fctr == 4) || ($fctr == 5))  # IP's
            {
              $ipaddr = inet_aton($f);
              $host = gethostbyaddr($ipaddr, AF_INET);
              if ($host)
              {
                print "    <td>" . $host . "</td>\n"; 
              }
              else
              {
                print "    <td>" . $f . "</td>\n"; 
              }
            }
            else
            {
              print "    <td>$f</td>\n";
            }
            $fctr += 1;
        }
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

