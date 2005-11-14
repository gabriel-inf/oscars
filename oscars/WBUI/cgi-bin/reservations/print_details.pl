#!/usr/bin/perl

# print_details.pl:  Prints the details of a reservation.
# Last modified:     November 13, 2005
# David Robertson    (dwrobertson@lbl.gov)
# Soo-yeon Hwang     (dapi@umich.edu)

#require '../lib/general.pl';

##############################################################################
# print_reservation_detail:  print details of reservation returned by SOAP
#                            call
# In:  user level, form parameters, and results of SOAP call 
# Out: None
#
sub print_reservation_detail {
    my( $form_params, $results, $msg, $starting_page ) = @_;

    my( $end_time );

    if ($row->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $row->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    my $src_port = $results->{reservation_src_port} || 'DEFAULT';
    my $dst_port = $results->{reservation_dst_port} || 'DEFAULT';
    my $protocol = $results->{reservation_protocol} || 'DEFAULT';
    my $dscp = $results->{reservation_dscp} || 'DEFAULT';
    print "<xml>";
    print "<msg>$msg</msg>";

    print qq{
    <div id="zebratable_ui">
    <p><strong>Reservation Details</strong></p>

    <table width="90%" id="reservationlist">
      <tr><td>Tag</td><td>$results->{reservation_tag}</td></tr>
      <tr><td>User</td><td>$form_params->{user_dn}</td></tr> 
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
          <td><a href="#" style="$starting_page/styleSheets/layout.css"
            onclick="return new_page('details',
            '$starting_page/cgi-bin/reservations/cancel.pl?reservation_id=$results->{reservation_id}')";
            >CANCEL</a></td></tr>
        };
    }

    print qq{
    </table>
    <form method="post" action="" onsubmit="return submit_form(this,
        'details', '$starting_page/cgi-bin/reservations/get_details.pl');">
    <input type="hidden" name="user_dn" value="$form_params->{user_dn}">
    </input>
    <input type="hidden" name="reservation_id"
           value="$form_params->{reservation_id}">
    </input>
    <p><input type="submit" value="Refresh"> 
    </input></p>
    </form>

    <p><a href="#" style="$starting_page/cgi-bin/reservations/list_form.pl"
          onclick="return new_page('list_form',
          '$starting_page/cgi-bin/reservations/list_form.pl');">
          $results->{user_last_name}
    <strong>Back to reservations list</strong></a></p>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print  "</xml>\n";
}
######

######
1;
