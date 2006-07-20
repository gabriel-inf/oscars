package OSCARS::WBUI::Method::CancelReservation;

=head1 NAME

OSCARS::WBUI::Method::CancelReservation - handles request to cancel an existing reservation

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::CancelReservation;

=head1 DESCRIPTION

Makes a SOAP request to cancel an existing reservation.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use Data::Dumper;
use SOAP::Lite;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# makeCall:  Make call to cancel reservation, and then make another call to
#            get the reservation details.
#
sub makeCall {
    my( $self, $params ) = @_;

    my $methodName = $self->{method};
    my $som = $self->docLiteralRequest($methodName, $params);

    $methodName = 'queryReservation';
    my $secondParams = {};
    $secondParams->{tag} = $params->{tag};
    $secondParams->{login} = $params->{login};
    return $self->docLiteralRequest($methodName, $secondParams);
} #___________________________________________________________________________ 


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
# In:   response from SOAP call
# Out:  None
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    my $details = OSCARS::WBUI::Method::ReservationDetails->new();
    return $details->output( $response );
} #____________________________________________________________________________


######
1;
