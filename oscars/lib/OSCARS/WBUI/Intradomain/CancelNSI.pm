#==============================================================================
package OSCARS::WBUI::Intradomain::CancelNSI;

=head1 NAME

OSCARS::WBUI::Intradomain::CancelNSI - handles request to cancel an existing reservation

=head1 SYNOPSIS

  use OSCARS::WBUI::Intradomain::CancelNSI;

=head1 DESCRIPTION

Makes a SOAP request to cancel an existing reservation.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 12, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Intradomain::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub output_div {
    my( $self, $results, $authorizations ) = @_;

    my $details = OSCARS::WBUI::Intradomain::ReservationDetails->new();
    return $details->output( $results, $authorizations );
} #____________________________________________________________________________


######
1;
