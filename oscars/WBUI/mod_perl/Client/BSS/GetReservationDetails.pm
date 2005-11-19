package Client::BSS::GetReservationDetails;

# Handles request to get details of a particular reservation.
#
# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

}
######

##############################################################################
# output:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output {
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

    print "<xml>\n";
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

######
1;
