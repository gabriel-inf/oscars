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

April 24, 2006

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

    my $params = $self->SUPER::modifyParams();
    my $f = DateTime::Format::W3CDTF->new();
    my $dt = DateTime->from_epoch( epoch => $params->{startTime} );
    my $offsetStr = $params->{origTimeZone};
    # strip out semicolon
    $offsetStr =~ s/://;
    my $timezone = DateTime::TimeZone->new( name => $offsetStr );
    $dt->set_time_zone($timezone);
    $params->{startTime} = $f->format_datetime($dt);

    $dt = DateTime->from_epoch( epoch => $params->{endTime} );
    $dt->set_time_zone($timezone);
    $params->{endTime} = $f->format_datetime($dt);
    return $params;
} #____________________________________________________________________________


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
