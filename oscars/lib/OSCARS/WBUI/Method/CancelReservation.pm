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

April 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


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
