#==============================================================================
package OSCARS::WBUI::Method::CreateReservation;

=head1 NAME

OSCARS::WBUI::Method::CreateReservation - handles request to create a reservation

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::CreateReservation;

=head1 DESCRIPTION

Makes a SOAP request to create a reservation.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::W3CDTF;
use Data::Dumper;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# modifyParams:  convert times from epoch seconds to xsd:datetime
#
sub modifyParams {
    my( $self ) = @_;

    my $request = $self->SUPER::modifyParams();
    my $f = DateTime::Format::W3CDTF->new();
    my $dt = DateTime->from_epoch( epoch => $request->{startTime} );
    my $offsetStr = $request->{origTimeZone};
    # strip out semicolon
    $offsetStr =~ s/://;
    my $timezone = DateTime::TimeZone->new( name => $offsetStr );
    $dt->set_time_zone($timezone);
    $request->{startTime} = $f->format_datetime($dt);

    $dt = DateTime->from_epoch( epoch => $request->{endTime} );
    $dt->set_time_zone($timezone);
    $request->{endTime} = $f->format_datetime($dt);
    return $request;
} #____________________________________________________________________________


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'ListReservations';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  print details of reservation returned by SOAP call
#    Different than ReservationDetails->output in that some information is not
#    returned that is already in the request.
# In:   response from SOAP call
# Out:  None
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    my $details = OSCARS::WBUI::Method::ReservationDetails->new();
    my $burstLimit = $request->{burstLimit} || 'DEFAULT';
    my $srcPort = $request->{srcPort} || 'DEFAULT';
    my $destPort = $request->{destPort} || 'DEFAULT';
    my $protocol = $request->{protocol} || 'DEFAULT';
    my $dscp = $request->{dscp} || 'DEFAULT';
    my $createdTime = $details->formatTime($response->{createdTime});
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
      <tr><td>User</td><td>$request->{login}</td></tr> 
      <tr><td>Description</td><td>$request->{description}</td></tr>
      <tr><td>Start time</td><td>$request->{startTime}</td></tr>
      <tr><td>End time</td><td>$request->{endTime}</td></tr>
      <tr><td>Created time</td><td>$createdTime</td></tr>
      <tr><td>Original time zone</td><td>$request->{origTimeZone}</td></tr>
      <tr><td>Bandwidth</td><td>$request->{bandwidth}</td></tr>
      <tr><td>Burst limit</td><td>$burstLimit</td></tr>
      <tr><td>Status</td><td>$response->{status}</td></tr>
      <tr><td>Source</td><td>$request->{srcHost}</td></tr>
      <tr><td>Destination</td><td>$request->{destHost}</td></tr>
      <tr><td>Source port</td><td>$srcPort</td></tr>
      <tr><td>Destination port</td><td>$destPort</td></tr>
      <tr><td>Protocol</td><td>$protocol</td></tr>
      <tr><td>DSCP</td><td>$dscp</td></tr>
    } );
    if ( $request->{class} ) {
        my $class = $request->{class} || 'DEFAULT';
        print( qq{ <tr><td>Class</td><td>$class</td></tr> } );
    }
    if ( $response->{path} ) {
        my $path = $response->{path};
        $path =~ s/ /, /g;
        print( qq{ <tr><td>Routers in path</td><td>$path</td></tr> } );
    }
    print( qq{
      </tbody>
    </table>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
