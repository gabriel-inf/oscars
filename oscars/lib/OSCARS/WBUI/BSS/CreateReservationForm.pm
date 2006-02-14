#==============================================================================
package OSCARS::WBUI::BSS::CreateReservationForm;

=head1 NAME

OSCARS::WBUI::BSS::CreateReservationForm - outputs reservation request form

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::CreateReservationForm;

=head1 DESCRIPTION

Handles engineer's request to display the create reservation form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 13, 2006

=cut

use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output:  resets method name and adds op name
#
sub modify_params {
    my( $self, $params ) = @_;

    $self->SUPER::modify_params($params);
    $params->{method} = 'ManageReservations';
    $params->{op} = 'createReservationForm';
} #____________________________________________________________________________


###############################################################################
# output:  prints out the reservation creation form
#          accessible from the "Reservations" notebook tab
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    my $params_str;

    my $user_dn = $self->{user_dn};
    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>Reservation creation form</msg> };
    $self->{tabs}->output('ManageReservations', $results->{authorizations});
    print qq{
    <div id='reservation-ui'>
    <form method='post' action='' onsubmit="return submit_form(this, 
               'server=BSS;method=ManageReservations;op=createReservation;',
	       check_reservation);">
      <input type='hidden' name='reservation_start_time'></input>
      <input type='hidden' name='reservation_end_time'></input>
      <input type='hidden' name='user_dn' value='$user_dn'></input>
      <input type='submit' value='Reserve bandwidth'></input>
      <input type='reset' value='Reset form fields'></input>

      <p>Required inputs are bordered in green.  Ranges or types of valid 
      entries are given in parentheses after the default values, if any. 
      If date and time fields are left blank, they are filled in with the 
      defaults.  The default time zone is your local time.</p>
    };

    if ($results->{authorizations}->{ChangeDefaultRouting}) {
        print qq{
          <p><strong>WARNING</strong>:  Entering a value in a red-outlined field 
	  may change default routing behavior for the selected flow.</p> };
    }
    print qq{
    <table>
      <tbody>
      <tr><td>Source</td>
        <td class='required'>
          <input type='text' name='source_host'></input></td>
        <td>(Host name or IP address)</td></tr>
      <tr><td>Source port</td>
        <td><input type='text' name='reservation_src_port' maxlength='5'> 
             </input>
        </td>
	<td>(1024-65535)</td></tr>
      <tr><td>Destination</td>
        <td class='required'>
          <input type='text' name='destination_host'></input></td>
        <td>(Host name or IP address)</td></tr>
      <tr><td>Destination port</td>
        <td><input type='text' name='reservation_dst_port' maxlength='5'>
	    </input></td>
	<td>(1024-65535)</td></tr>
      <tr><td>Bandwidth (Mbps)</td>
        <td class='required'>
          <input type='text' name='reservation_bandwidth' maxlength='7'>
          </input>
	</td>
        <td>(10-10000)</td></tr>
      <tr><td>Protocol</td>
        <td><input type='text' name='reservation_protocol'></input></td>
	<td>(0-255, or string)</td></tr>
      <tr><td>Differentiated service code point</td>
        <td><input type='text' name='reservation_dscp' maxlength='2'>
	    </input></td>
	<td>(0-63)</td></tr>
      <tr><td>Purpose of reservation</td>
        <td class='required'><textarea name='reservation_description' rows='4' 
            cols='25'> </textarea></td>
        <td>(For our records)</td></tr>

    };
    if ($results->{authorizations}->{ChangeDefaultRouting}) {
      print qq{
      <tr>
        <td>Ingress loopback</td>
        <td class='warning'>
	    <input type='text' name='ingress_router'></input>
        </td>
	<td>(Host name or IP address)</td>
	</tr>
      <tr>
        <td>Egress loopback</td>
        <td class='warning'>
	    <input type='text' name='egress_router'></input>
        </td>
	<td>(Host name or IP address)</td>
      </tr>
      };
    }
    my @local_settings = localtime();
    my $year = $local_settings[5] + 1900;
    my $month = $local_settings[4] + 1;

    my $date = $local_settings[3];
    my $hour = $local_settings[2];
    my $minute = $local_settings[1];
    print qq{
      <tr><td>Year</td>
        <td><input type='text' name='start_year' maxlength='4'></input></td>
        <td>$year</td></tr>
      <tr><td>Month</td>
        <td><input type='text' name='start_month' maxlength='2'></input></td>
	<td>$month (1-12)</td></tr>
	<tr><td>Date</td>
        <td><input type='text' name='start_date' maxlength='2'></input></td>
	<td>$date (1-31)</td></tr>

      <tr><td>UTC offset</td>
        <td id="time-zone-options"> </td>
	<td id="local-time-zone"> </td>
      </tr>

      <tr><td>Hour</td>
        <td><input type='text' name='start_hour' maxlength='2'></input></td>
	<td>$hour (0-23)</td></tr>
      <tr><td>Minute</td>
        <td><input type='text' name='start_minute' maxlength='2'></input></td>
	<td>$minute (0-59)</td></tr>
      <tr><td>Duration (Hours)</td>
        <td><input type='text' name='duration_hour' maxlength='16'></input>
       	</td>
	<td>0.01 (0.01 to Indefinite)</td></tr>
    };
    if ($results->{authorizations}->{PersistentReservation}) {
        print qq{
      <tr><td>Persistent reservation</td>
        <td><input type='checkbox' name='persistent' size='8' value='0'></input></td>
	<td>Doesn't expire until explicitly cancelled.</td></tr> };
    }
    print qq{ </tbody></table></form></div> };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
