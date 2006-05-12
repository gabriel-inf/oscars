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

May 5, 2006

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
    my( $self, $soapServer, $params ) = @_;

    my $method = $self->{method};
    $method =~ s/(\w)/\l$1/;
    my $request = { $method . "Request" => $params };
    my $som = $soapServer->$method($request);
    my $secondParams = {};
    $method = 'queryReservation';
    $secondParams->{tag} = $request->{tag};
    $secondParams->{login} = $request->{login};
    $request = { $method . "Request" => $secondParams };
    my $secondSom = $soapServer->queryReservation($request);
    return $secondSom;
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
