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

July 15, 2006

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
# In:   response from SOAP call
# Out:  None
#
sub output {
    my( $self, $response ) = @_;

    my $srcPort = $response->{srcPort} || 'DEFAULT';
    my $destPort = $response->{destPort} || 'DEFAULT';
    my $protocol = $response->{protocol} || 'DEFAULT';
    my $dscp = $response->{dscp} || 'DEFAULT';
    my $startTime = $self->formatTime($response->{startTime});
    my $endTime = $self->formatTime($response->{endTime});
    my $createdTime = $self->formatTime($response->{createdTime});
    my $origTimeZone = $self->getTimeZone($response->{createdTime});
    my $path = $response->{path};
    $path =~ s/ /, /g;
    my $msg = "Successfully got reservation details.";
    print( qq{
    <p><strong>Reservation Details</strong></p>
    <p>To return to the reservations list, click on the Reservations tab.</p>
    <p>
    } );

    if (($response->{status} eq 'pending') ||
        ($response->{status} eq 'active')) {
        my $cancelSubmitStr = "return submitForm(this,
             'method=CancelReservation;');";
        print( qq{
        <form method="post" action="" onsubmit="$cancelSubmitStr">
        <input type='hidden' class='SOAP' name='tag' value="$response->{tag}"></input>
        <input type='submit' value='CANCEL'></input>
        </form>
        } );
    }

    my $refreshSubmitStr = "return submitForm(this,
             'method=QueryReservation;');";
    print( qq{
    <form method="post" action="" onsubmit="$refreshSubmitStr">
    <input type='hidden' class='SOAP' name='tag' value="$response->{tag}"></input>
    <input type='submit' value='Refresh'>
    </input>
    </form>
    </p>
    <table width='90%' class='sortable'>
      <thead><tr><td>Attribute</td><td>Value</td></tr></thead>
      <tbody>
      <tr><td>Tag</td><td>$response->{tag}</td></tr>
      <tr><td>User</td><td>$response->{login}</td></tr> 
      <tr><td>Description</td><td>$response->{description}</td></tr>
      <tr><td>Start time</td><td>$startTime</td></tr>
      <tr><td>End time</td><td>$endTime</td></tr>
      <tr><td>Created time</td><td>$createdTime</td></tr>
      <tr><td>Original time zone</td><td>$origTimeZone</td></tr>
      <tr><td>Bandwidth</td><td>$response->{bandwidth}</td></tr>
      <tr><td>Burst limit</td><td>$response->{burstLimit}</td></tr>
      <tr><td>Status</td><td>$response->{status}</td></tr>
      <tr><td>Source</td><td>$response->{srcHost}</td></tr>
      <tr><td>Destination</td><td>$response->{destHost}</td></tr>
      <tr><td>Source port</td><td>$srcPort</td></tr>
      <tr><td>Destination port</td><td>$destPort</td></tr>
      <tr><td>Protocol</td><td>$protocol</td></tr>
      <tr><td>DSCP</td><td>$dscp</td></tr>
    } );
    if ( $response->{class} ) {
        print( qq{
        <tr><td>Class</td><td>$response->{class}</td></tr>
        <tr><td>Routers in path</td><td>$path</td></tr>
        } );
    }
    print( qq{
      </tbody>
    </table>
    } );
    return $msg;
} #____________________________________________________________________________


###############################################################################
# formatTime:  formats xsd:datetime for output
#
sub formatTime {
    my( $self, $datetime ) = @_;

    my @datetimeSegments = split('T', $datetime);
    my @timeSegment = split(':', $datetimeSegments[1]);
    my $formattedTime =
        $datetimeSegments[0] . ' ' . $timeSegment[0] . ':' . $timeSegment[1];
    return $formattedTime;
} #___________________________________________________________________________ 


###############################################################################
# getTimeZone:  gets time zone from xsd:datetime string
#
sub getTimeZone {
    my( $self, $datetime ) = @_;

    return substr($datetime, -6, 6);
} #___________________________________________________________________________ 


######
1;
