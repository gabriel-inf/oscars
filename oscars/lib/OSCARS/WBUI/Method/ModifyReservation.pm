#==============================================================================
package OSCARS::WBUI::Method::ModifyReservation;

=head1 NAME

OSCARS::WBUI::Method::ModifyReservation - handles request to modify an existing reservation

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ModifyReservation;

=head1 DESCRIPTION

Makes a SOAP request to modify a reservation, given its tag.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  print details of reservation returned by SOAP call
# In:   response from SOAP call
# Out:  None
#
sub outputDiv {
    my( $self, $request, $response, $authorizations ) = @_;

    my $details = OSCARS::WBUI::Method::ReservationDetails->new();
    return $details->output( $response, $authorizations );
} #____________________________________________________________________________


######
1;
