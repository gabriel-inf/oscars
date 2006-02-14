#==============================================================================
package OSCARS::WBUI::BSS::EngrReservationForm;

=head1 NAME

OSCARS::WBUI::BSS::EngrReservationForm - outputs reservation request form

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::EngrReservationForm;

=head1 DESCRIPTION

Handles engineer's request to display the create reservation form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 28, 2006

=cut

use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# Currently a noop.
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    return {};
} #____________________________________________________________________________


###############################################################################
# output:  Prints out the reservation creation form as accessed by an engineer.
#
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    my $params_str;

    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>Reservation creation form</msg> };
    $self->{tabs}->output('ManageReservations', $results->{authorizations});
    print qq{
    <div id='reservation-ui'>
    <form method='post' action='' onsubmit="return submit_form(this, 
               'server=BSS;method=ManageReservations;op=createReservation;');">
      <input type='hidden' name='reservation_start_time'></input>
      <input type='hidden' name='reservation_end_time'></input>
      <input type='hidden' name='user_dn' value="$self->{user_dn}"></input>
      <input type='submit' value='Reserve bandwidth'></input>
      <input type='reset' value='Reset form fields'></input>

      <p>Required inputs are bordered in green.  Ranges or types of valid 
      entries are given in parentheses below the input fields.</p>

      <table>
        <tr>
          <th>Source</th><th>Destination</th><th>Bandwidth (Mbps)</th>
        </tr>
        <tr>
          <td class='required'>
            <input type='text' name='source_host' size='29'></input></td>
          <td class='required'>
            <input type='text' name='destination_host' size='29'></input></td>
          <td class='required'>
            <input type='text' name='reservation_bandwidth' maxlength='7' 
                   size='14'></input></td>
        </tr>
        <tr>
          <td>(Host name or IP address)</td> <td>(Host name or IP address)</td>
          <td>(10-10000)</td>
        </tr>
      </table>
      <p>Indicate the starting date and time, and the duration in hours, of 
      your reservation.  Fields left blank will default to the examples below 
      the input fields.  The default time zone is the local time.
      Checking the <strong>Persistent</strong> box makes a reservation's 
      duration indefinite, overriding the duration field.</p>
      <table>
      <tr><th>Year</th><th>Month</th><th>Date</th><th>Hour</th><th>Minute</th>
          <th>UTC offset</th><th>Duration (Hours)</th><th>Persistent</th>
      </tr>
      <tr>
        <td> <input type='text' name='start_year' size='6' maxlength='4'>
             </input>
        </td>
        <td>
          <input type='text' name='start_month' size='6' maxlength='2'></input>
        </td>
        <td>
          <input type='text' name='start_date' size='6' maxlength='2'></input>
        </td>
        <td>
          <input type='text' name='start_hour' size='6' maxlength='2'></input>
        </td>
        <td>
          <input type='text' name='start_minute' size='6' maxlength='2'> 
          </input>
        </td>
        <td id='tz-option-list'></td>
        <td>
          <input type='text' name='duration_hour' size='10' maxlength='16'>
          </input>
        </td>
        <td>
          <input type='checkbox' name='persistent' value='0'></input>";
        </td>
      </tr>
      <tr id='time-settings-example'>
        <td colspan='8'></td>
      </tr>
      <tr>
        <td> </td><td>(1-12)</td><td>(1-31)</td><td>(0-23)</td><td>(0-59)</td>
        <td> </td><td>(0.01-INF)</td><td> </td>
      </tr>
    </table>

    <p>Please let us know the purpose of making this reservation.</p>
    <table cols='1'>
      <tr>
        <td class='required'><textarea name='reservation_description' rows='2' 
            cols='98'> </textarea></td>
      </tr>
    </table>

    <p>The following are optional fields.  <strong>DSCP</strong> sets the 
    differentiated services code point.</p>

    <table cols='4'>
      <tr><td colspan='4'> </td></tr>
      <tr><th>Source port</th> <th>Destination port</th> <th>Protocol</th>
          <th>DSCP</th>
      </tr>
      <tr>
        <td><input type='text' name='reservation_src_port' maxlength='5' 
                   size='17'></input></td>
        <td><input type='text' name='reservation_dst_port' maxlength='5'
                   size='17'></input></td>
        <td><input type='text' name='reservation_protocol' 
                   size='17'></input></td>
        <td><input type='text' name='reservation_dscp' maxlength='2' 
                   size='17'></input></td>
      </tr>
      <tr>
        <td>(1024-65535)</td><td>(1024-65535)</td><td>(0-255), or string</td>
        <td>(0-63)</td>
      </tr>
    </table>

    <p>
    <strong>WARNING</strong>:  Entries in the following fields may change 
    default routing behavior for the selected flow.</p>

    <table cols='2'>
      <tr><td colspan='2'></td></tr>
      <tr><th>Ingress loopback</th><th>Egress loopback</th></tr>
      <tr>
        <td><input type='text' name='ingress_router'></input></td>
        <td><input type='text' name='egress_router'></input></td>
      </tr>
      <tr><td>(Host name or IP address)</td><td>(Host name or IP address)</td>
      </tr>
    </table>
    </form>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
