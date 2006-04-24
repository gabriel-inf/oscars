#==============================================================================
package OSCARS::WBUI::Method::ReservationDetails;

=head1 NAME

OSCARS::WBUI::Method::ReservationDetails - outputs reservation details

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ReservationDetails;

=head1 DESCRIPTION

Library method for CancelReservation, CreateReservation, QueryReservation, and ModifyReservation output.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 22, 2006

=cut


use strict;

use Data::Dumper;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# output:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results, $authorizations ) = @_;

    my $endTime;

    if ($results->{endTime} ne '2039-01-01 00:00:00') {
        $endTime = $results->{endTime};
    }
    else { $endTime = 'PERSISTENT'; }
    my $srcPort = $results->{srcPort} || 'DEFAULT';
    my $destPort = $results->{destPort} || 'DEFAULT';
    my $protocol = $results->{protocol} || 'DEFAULT';
    my $dscp = $results->{dscp} || 'DEFAULT';

    my $msg = "Successfully got reservation details.";
    print( qq{
    <div>
    <p><strong>Reservation Details</strong></p>
    <p>To return to the reservations list, click on the Reservations tab.</p>
    <p>
    } );

    if (($results->{status} eq 'pending') ||
        ($results->{status} eq 'active')) {
        my $cancelSubmitStr = "return submit_form(this,
             'method=CancelReservation;');";
        print( qq{
        <form method="post" action="" onsubmit="$cancelSubmitStr">
        <input type='hidden' class='SOAP' name='id' value="$results->{id}"></input>
        <input type='submit' value='CANCEL'></input>
        </form>
        } );
    }

    my $refreshSubmitStr = "return submit_form(this,
             'method=QueryReservation;');";
    print( qq{
    <form method="post" action="" onsubmit="$refreshSubmitStr">
    <input type='hidden' class='SOAP' name='id' value="$results->{id}"></input>
    <input type='submit' value='Refresh'>
    </input>
    </form>
    </p>
    <table width='90%' class='sortable'>
      <thead><tr><td>Attribute</td><td>Value</td></tr></thead>
      <tbody>
      <tr><td>Tag</td><td>$results->{tag}</td></tr>
      <tr><td>User</td><td>$results->{login}</td></tr> 
      <tr><td>Description</td><td>$results->{description}</td></tr>
      <tr><td>Start time</td><td>$results->{startTime}</td></tr>
      <tr><td>End time</td><td>$endTime</td></tr>
      <tr><td>Created time</td><td>$results->{createdTime}</td></tr>
      <tr><td>Bandwidth</td><td>$results->{bandwidth}</td></tr>
      <tr><td>Burst limit</td><td>$results->{burstLimit}</td></tr>
      <tr><td>Status</td><td>$results->{status}</td></tr>
      <tr><td>Source</td><td>$results->{srcHost}</td></tr>
      <tr><td>Destination</td><td>$results->{destHost}</td></tr>
      <tr><td>Source port</td><td>$srcPort</td></tr>
      <tr><td>Destination port</td><td>$destPort</td></tr>
      <tr><td>Protocol</td><td>$protocol</td></tr>
      <tr><td>DSCP</td><td>$dscp</td></tr>
    } );
    if ( $authorizations->{ManageDomains} ) {
        print( qq{
        <tr><td>Class</td><td>$results->{class}</td></tr>
        <tr><td>Ingress router</td><td>$results->{ingressRouter}</td></tr>
        <tr><td>Ingress loopback</td><td>$results->{ingressIP}</td></tr>
        <tr><td>Egress router</td><td>$results->{egressRouter}</td></tr>
        <tr><td>Egress loopback</td><td>$results->{egressIP}</td></tr>
        <tr><td>Routers in path</td>
        <td>
        } );
        my $pathStr = '';
        for $_ (@{$results->{path}}) {
            $pathStr .= $_ . ' - ';
        }
        # remove last '-'
        substr($pathStr, -3, 3) = '';
        print("$pathStr </td> </tr>");
    }
    print( qq{
      </tbody>
    </table></div>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
