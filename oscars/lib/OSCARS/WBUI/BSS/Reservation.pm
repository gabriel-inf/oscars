#==============================================================================
package OSCARS::WBUI::BSS::Reservation;

=head1 NAME

OSCARS::WBUI::BSS::Reservation - handles request to view a reservation's details

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::Reservation;

=head1 DESCRIPTION

Makes a SOAP request to view a particular reservation's details.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output_div {
    my( $self, $results ) = @_;

    my $end_time;

    if ($results->{reservation_end_time} ne '2039-01-01 00:00:00') {
        $end_time = $results->{reservation_end_time};
    }
    else { $end_time = 'PERSISTENT'; }
    my $src_port = $results->{reservation_src_port} || 'DEFAULT';
    my $dst_port = $results->{reservation_dst_port} || 'DEFAULT';
    my $protocol = $results->{reservation_protocol} || 'DEFAULT';
    my $dscp = $results->{reservation_dscp} || 'DEFAULT';

    my $msg = "Successfully got reservation details.";
    print( qq{
    <div>
    <p><strong>Reservation Details</strong></p>
    <p>To return to the reservations list, click on the Reservations tab.</p>
    <p>
    } );

    if (($results->{reservation_status} eq 'pending') ||
        ($results->{reservation_status} eq 'active')) {
        my $cancel_submit_str = "return submit_form(this,
             'server=BSS;method=ManageReservations;op=cancelReservation;');";
        print( qq{
        <form method="post" action="" onsubmit="$cancel_submit_str">
        <input type='hidden' name='reservation_id'
           value="$results->{reservation_id}"></input>
        <input type='submit' value='CANCEL'>
	</input>
        </form>
        } );
    }

    my $refresh_submit_str = "return submit_form(this,
             'server=BSS;method=Reservation;');";
    print( qq{
    <form method="post" action="" onsubmit="$refresh_submit_str">
    <input type='hidden' name='reservation_id'
           value="$results->{reservation_id}"></input>
    <input type='submit' value='Refresh'>
    </input>
    </form>
    </p>
    <table width='90%' class='sortable'>
      <thead><tr><td>Attribute</td><td>Value</td></tr></thead>
      <tbody>
      <tr><td>Tag</td><td>$results->{reservation_tag}</td></tr>
      <tr><td>User</td><td>$results->{user_login}</td></tr> 
      <tr><td>Description</td><td>$results->{reservation_description}</td></tr>
      <tr><td>Start time</td><td>$results->{reservation_start_time}</td></tr>
      <tr><td>End time</td><td>$end_time</td></tr>
      <tr><td>Created time</td><td>$results->{reservation_created_time}</td></tr>
      <tr><td>Bandwidth</td><td>$results->{reservation_bandwidth}</td></tr>
      <tr><td>Burst limit</td><td>$results->{reservation_burst_limit}</td></tr>
      <tr><td>Status</td><td>$results->{reservation_status}</td></tr>
      <tr><td>Source</td><td>$results->{source_host}</td></tr>
      <tr><td>Destination</td><td>$results->{destination_host}</td></tr>
      <tr><td>Source port</td><td>$src_port</td></tr>
      <tr><td>Destination port</td><td>$dst_port</td></tr>
      <tr><td>Protocol</td><td>$protocol</td></tr>
      <tr><td>DSCP</td><td>$dscp</td></tr>
    } );
    if ( $results->{authorizations}->{ChangeDefaultRouting} ) {
        print( qq{
        <tr><td>Class</td><td>$results->{reservation_class}</td></tr>
        <tr><td>Ingress router</td><td>$results->{ingress_router}</td></tr>
        <tr><td>Ingress loopback</td><td>$results->{ingress_ip}</td></tr>
        <tr><td>Egress router</td><td>$results->{egress_router}</td></tr>
        <tr><td>Egress loopback</td><td>$results->{egress_ip}</td></tr>
        <tr><td>Routers in path</td>
        <td>
        } );
        my $path_str = '';
        for $_ (@{$results->{reservation_path}}) {
            $path_str .= $_ . ' - ';
        }
        # remove last '-'
        substr($path_str, -3, 3) = '';
        print("$path_str </td> </tr>");
    }
    print( qq{
      </tbody>
    </table></div>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
