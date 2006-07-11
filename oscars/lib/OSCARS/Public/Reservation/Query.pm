#==============================================================================
package OSCARS::Public::Reservation::Query;

=head1 NAME

OSCARS::Public::Reservation::Query- SOAP method to list the details of a
specific reservation.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Query;

=head1 DESCRIPTION

SOAP method that returns the details of a reservation
from the reservations table.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

July 10, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                                 'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  get reservation details from the database, given its
#     reservation tag.  If a user has the 'manage' permission on the
#     'Reservations' resource, they can query any reservation's details.
#     Otherwise they can only list reservations that they have made, with less
#     of the details.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my @strArray = split('-', $request->{tag});
    my $id = $strArray[-1];
    return $self->{reservation}->queryReservationResponse($id);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
