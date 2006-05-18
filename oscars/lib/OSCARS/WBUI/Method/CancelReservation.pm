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

May 17, 2006

=cut


use strict;

use Data::Dumper;
use SOAP::Lite;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# makeCall:  Make call to cancel reservation, and then make another call to
#            get the reservation details (updates at less than main div level 
#            not implemented yet).
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
# outputDiv:  print details of reservation returned by SOAP call
# In:   response from SOAP call
# Out:  None
#
sub outputDiv {
    my( $self, $response, $authorizations ) = @_;

    my $details = OSCARS::WBUI::Method::ReservationDetails->new();
    return $details->output( $response, $authorizations );
} #____________________________________________________________________________


######
1;
