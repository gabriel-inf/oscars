package Client::BSSCallbacks;

# Callbacks for SOAPAdapter for various phases of OSCARS request to BSS.
# Will be converted to something more object-oriented.
#
# Last modified:  November 17, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

###############################################################################
# reservation_form_output:  prints out the reservation creation form
#                          accessible from the "Make a Reservation" notebook tab
# In:   results of SOAP call
# Out:  None
#
sub reservation_form_output {
    my( $results ) = @_;

    my $params_str;
    print qq{
    <msg>Reservation creation form</msg>
    <div id="reservation_ui">
    <form method="post" action=""
      onsubmit="return submit_form(this, '/perl/adapt.pl?method=insert',
                                   '$params_str');">
      <input type="hidden" name="reservation_start_time"></input>
      <input type="hidden" name="reservation_end_time"></input>
      <input type="hidden" name="user_dn" value="$results->{user_dn}">
      </input>
      <input type="submit" value="Reserve bandwidth"></input>
      <input type="reset" value="Reset form fields"></input>

      <p>Required inputs are bordered in green.  Ranges or types of valid 
      entries are given in parentheses below the input fields.</p>

      <table>
        <tr>
          <th>Source</th> <th>Destination</th> <th>Bandwidth (Mbps)</th>
        </tr>
        <tr>
          <td class="required">
            <input type="text" name="source_host" size="29"></input></td>
          <td class="required">
            <input type="text" name="destination_host" size="29"></input></td>
          <td class="required">
            <input type="text" name="reservation_bandwidth" maxlength="7" 
                   size="14"></input></td>
        </tr>
        <tr>
          <td>(Host name or IP address)</td> <td>(Host name or IP address)</td>
          <td>(10-10000)</td>
        </tr>
      </table>
      <p>Indicate the starting date and time, and the duration in hours, of 
      your reservation.  Fields left blank will default to the examples below 
      the input fields.  The default time zone is the local time.
    };
    if (authorized($results->{user_level}, "engr")) {
        print qq{
          Checking the <strong>Persistent</strong> box makes a reservation's 
          duration indefinite, overriding the duration field.
        };
    }
    print qq{
      </p>
      <table>
        <tr><th>Year</th>   <th>Month</th>      <th>Date</th> <th>Hour</th>
            <th>Minute</th> <th>UTC offset</th> <th>Duration (Hours)</th>
    };
    if (authorized($results->{user_level}, "engr")) {
        print "<th>Persistent</th>";
    }
    else { print "<th> </th>"; }
    print qq{
      </tr>
      <tr>
        <td>
          <input type="text" name="start_year" size="6" maxlength="4"></input>
        </td>
        <td>
          <input type="text" name="start_month" size="6" maxlength="2"></input>
        </td>
        <td>
          <input type="text" name="start_date" size="6" maxlength="2"></input>
        </td>
        <td>
          <input type="text" name="start_hour" size="6" maxlength="2"></input>
        </td>
        <td>
          <input type="text" name="start_minute" size="6" maxlength="2">
          </input>
        </td>
        <td id="tz_option_list"></td>
        <td>
          <input type="text" name="duration_hour" size="10" maxlength="16">
          </input>
        </td>
        <td>
    };
    if (authorized($results->{user_level}, "engr")) {
        print '  <input type="checkbox" name="persistent" value="0"></input>';
    }
    print qq{
        </td>
      </tr>
      <tr id="time_settings_example">
        <td colspan="8"></td>
      </tr>
      <tr>
        <td> </td><td>(1-12)</td><td>(1-31)</td><td>(0-23)</td><td>(0-59)</td>
        <td> </td><td>(0.01-INF)</td><td> </td>
      </tr>
    </table>

    <p>Please let us know the purpose of making this reservation.</p>
    <table cols="1">
      <tr>
        <td class="required"><textarea name="reservation_description" rows="2" 
            cols="98"> </textarea></td>
      </tr>
    </table>

    <p>The following are optional fields.  <strong>DSCP</strong> sets the 
    differentiated services code point.</p>

    <table cols="4">
      <tr><td colspan="4"> </td></tr>
      <tr><th>Source port</th> <th>Destination port</th> <th>Protocol</th>
          <th>DSCP</th>
      </tr>
      <tr>
        <td><input type="text" name="reservation_src_port" maxlength="5" 
                   size="17"></input></td>
        <td><input type="text" name="reservation_dst_port" maxlength="5"
                   size="17"></input></td>
        <td><input type="text" name="reservation_protocol" 
                   size="17"></input></td>
        <td><input type="text" name="reservation_dscp" maxlength="2" 
                   size="17"></input></td>
      </tr>
      <tr>
        <td>(1024-65535)</td> <td>(1024-65535)</td> <td>(0-255), or string</td>
        <td>(0-63)</td>
      </tr>
    </table>
    };

    if (authorized($results->{user_level}, "engr")) {
        print qq{
    <p>
    <strong>WARNING</strong>:  Entries in the following fields may change 
    default routing behavior for the selected flow.</p>

    <table cols="2">
      <tr><td colspan="2"></td></tr>
      <tr><th>Ingress loopback</th><th>Egress loopback</th></tr>
      <tr>
        <td><input type="text" name="ingress_router"></input></td>
        <td><input type="text" name="egress_router"></input></td>
      </tr>
      <tr><td>(Host name or IP address)</td><td>(Host name or IP address)</td>
      </tr>
    </table>
    </form>
    </div>
      };
    }
}
######

##############################################################################
# reservation_list_output:  print list of all reservations if the caller has 
#               admin privileges, otherwise just print that user's reservations
# In:   results of SOAP call
# Out:  None
#
sub reservation_list_output {
    my ( $results ) = @_;

    my $params_str;
    print qq{
    <msg>Successfully retrieved reservations.</msg>
    <div id="zebratable_ui">
    <p>Click on a column header to sort by that column. Times given are in the
    time zone of the browser.  Click on the Reservation Tag link to view
    detailed information about the reservation.</p>

    <p><form method="post" action="" onsubmit="return submit_form(this,
            '/perl/adapt.pl?method=list_form', '$params_str');">
    <input type="submit" value="Refresh"></input>
    <input type="hidden" name="user_dn" value="$results->{user_dn}">
    </input>
    </form></p>

    <table cellspacing="0" width="90%" class="sortable" id="reservationlist">
    <thead>
      <tr><td>Tag</td><td>Start Time</td><td>End Time</td><td>Status</td>
          <td>Origin</td><td>Destination</td>
      </tr>
    </thead>

    <tbody>
    };
    for my $row (@$results) { print_row( $row ); }
    print qq{
    </tbody>
    </table>

    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
}
######

##############################################################################
# output_reservation_row:  print the table row corresponding to one reservation
#
# In:   one row of results from SOAP call
# Out:  None
#
sub output_reservation_row {
    my( $row ) = @_;

    my( $end_time );

    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $row->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    print qq{
    <tr>
      <td>
      <a href="#" style="/styleSheets/layout.css"
       onclick="return new_page(
          '/perl/adapt.pl?method=get_details;reservation_id=$row->{reservation_id}');"
          >$row->{reservation_tag}</a>
      </td>
      <td>$row->{reservation_start_time}</td>
      <td>$end_time</td>
      <td>$row->{reservation_status}</td>
      <td>$row->{source_host}</td>
      <td>$row->{destination_host}</td>
    </tr>
    };
}
######


##############################################################################
# reservation_details_otuput:  print details of reservation returned by SOAP
#                              call
# In:   results of SOAP call
# Out:  None
#
sub reservation_details_output {
    my( $results ) = @_;

    my( $end_time );

    my $params_str;
    if ($results->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $results->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    my $src_port = $results->{reservation_src_port} || 'DEFAULT';
    my $dst_port = $results->{reservation_dst_port} || 'DEFAULT';
    my $protocol = $results->{reservation_protocol} || 'DEFAULT';
    my $dscp = $results->{reservation_dscp} || 'DEFAULT';

    print qq{
    <msg>Successfully got reservation details.</msg>
    <div id="zebratable_ui">
    <p><strong>Reservation Details</strong></p>

    <table width="90%" id="reservationlist">
      <tr><td>Tag</td><td>$results->{reservation_tag}</td></tr>
      <tr><td>User</td><td>$results->{user_dn}</td></tr> 
      <tr><td>Description</td><td>$results->{reservation_description}</tr>
      <tr><td>Start time</td><td>$results->{reservation_start_time}</td></tr>
      <tr><td>End time</td><td>$end_time</td></tr>
      <tr><td>Created time</td><td>$results->{reservation_created_time}
        </td></tr>
      <tr><td>Bandwidth</td><td>$results->{reservation_bandwidth}</td></tr>
      <tr><td>Burst limit</td><td>$results->{reservation_burst_limit}</td></tr>
      <tr><td>Status</td><td>$results->{reservation_status}</td></tr>
      <tr><td>Source</td><td>$results->{source_host}</td></tr>
      <tr><td>Destination</td><td>$results->{destination_host}</td></tr>
      <tr><td>Source port</td><td>$src_port</td></tr>
      <tr><td>Destination port</td>$dst_port</td></tr>
      <tr><td>Protocol</td><td>$protocol</td></tr>
      <tr><td>DSCP</td><td>$dscp</td></tr>
    };
    # TODO:  AAAS must undef these if user doesn't have authorization to set
    #        If one of these isn't set, none are set
    if ( $results->{reservaton_class} ) {
        print qq{
        <tr><td>Class</td><td>$results->{reservation_class}</td></tr>
        <tr><td>Ingress router</td><td>$results->{ingress_router}</td></tr>
        <tr><td>Ingress loopback</td><td>$results->{ingress_ip}</td></tr>
        <tr><td>Egress router</td><td>$results->{egress_router}</td></tr>
        <tr><td>Egress loopback</td><td>$results->{egress_ip}</td></tr>
        <tr><td>Routers in path</td><td>
        };
        my $path_str = "";
        for $_ (@{$results->{reservation_path}}) {
            $path_str .= $_ . " -> ";
        }
        # remove last '->'
        substr($path_str, -4, 4) = '';
        print qq{
        $path_str</td></tr>
        };
    }

    if (($results->{reservation_status} eq 'pending') ||
        ($results->{reservation_status} eq 'active')) {
        print qq{
          <td>Action: </td>
          <td><a href="#" style="/styleSheets/layout.css"
            onclick="return new_page(
            '/perl/adapt.pl?method=cancel;reservation_id=$results->{reservation_id}')";
            >CANCEL</a></td></tr>
        };
    }

    print qq{
    </table>
    <form method="post" action="" onsubmit="return submit_form(this,
                                  '/perl/adapt.pl?method=get_details',
                                  '$params_str');">
    <input type="hidden" name="user_dn" value="$results->{user_dn}">
    </input>
    <input type="hidden" name="reservation_id"
           value="$results->{reservation_id}">
    </input>
    <p><input type="submit" value="Refresh"> 
    </input></p>
    </form>

    <p><a href="#" style="/perl/adapt.pl?method=list_form"
          onclick="return new_page('/perl/adapt.pl?method=list_form');">
          $results->{user_last_name}
    <strong>Back to reservations list</strong></a></p>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print  "</xml>\n";
}
######
