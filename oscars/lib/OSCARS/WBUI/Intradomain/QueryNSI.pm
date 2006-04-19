#==============================================================================
package OSCARS::WBUI::Intradomain::QueryNSI;

=head1 NAME

OSCARS::WBUI::Intradomain::QueryNSI - handles request to view a reservation's details

=head1 SYNOPSIS

  use OSCARS::WBUI::Intradomain::QueryNSI;

=head1 DESCRIPTION

Makes a SOAP request to view a particular reservation's details.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Intradomain::ReservationDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  print details of reservation returned by SOAP call
# In:   results of SOAP call
# Out:  None
#
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    my $details = OSCARS::WBUI::Intradomain::ReservationDetails->new();
    return $details->output( $results, $authorizations );
} #____________________________________________________________________________


######
1;
