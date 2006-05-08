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

May 5, 2006

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

    my $request = $self->SUPER::modifyParams();
    my $f = DateTime::Format::W3CDTF->new();
    my $dt = DateTime->from_epoch( epoch => $request->{startTime} );
    my $offsetStr = $request->{origTimeZone};
    # strip out semicolon
    $offsetStr =~ s/://;
    my $timezone = DateTime::TimeZone->new( name => $offsetStr );
    $dt->set_time_zone($timezone);
    $request->{startTime} = $f->format_datetime($dt);

    $dt = DateTime->from_epoch( epoch => $request->{endTime} );
    $dt->set_time_zone($timezone);
    $request->{endTime} = $f->format_datetime($dt);
    return $request;
} #____________________________________________________________________________


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
