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

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


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
    return( $details->output( $response ) );
} #____________________________________________________________________________


######
1;
