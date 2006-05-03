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

May 3, 2006

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
    my( $self, $soapServer, $soapParams ) = @_;

    $soapParams->{method} =~ s/(\w)/\l$1/;
    my $method = $soapParams->{method};
    my $som = $soapServer->$method($soapParams);
    $soapParams->{method} =~ s/(\w)/\U$1/;
    my $secondParams = {};
    $secondParams->{method} = 'queryReservation';
    $secondParams->{tag} = $soapParams->{tag};
    $secondParams->{login} = $soapParams->{login};
    my $secondSom = $soapServer->queryReservation($secondParams);
    return $secondSom;
} #___________________________________________________________________________ 


###############################################################################
# outputDiv:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    my $details = OSCARS::WBUI::Method::ReservationDetails->new();
    return $details->output( $results, $authorizations );
} #____________________________________________________________________________


######
1;
