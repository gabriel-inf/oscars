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

July 20, 2006

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
    return $self->queryReservation($id);
} #____________________________________________________________________________


###############################################################################
# queryReservation:  get reservation details from the database, given 
#     its reservation id.  If a user has the proper authorization, he can view 
#     any reservation's details.  Otherwise he can only view reservations that
#     he has made, with less of the details.  If a database field is NULL
#     or blank, it is not returned.
#
# In:  reference to hash of parameters
# Out: reference to hash of reservation details
#
sub queryReservation {
    my( $self, $id ) = @_;

    my( $statement, $fields );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationAuthDetails WHERE id = ?';
        $fields = $self->{db}->getRow($statement, $id);
    }
    else {
        $statement = 'SELECT * FROM ReservationUserDetails ' .
                     'WHERE login = ? AND id = ?';
        $fields = $self->{db}->getRow($statement, $self->{user}->{login}, $id);
    }
    if (!$fields) { return undef; }
    my $resDetails = $self->{reservation}->format($fields);
    return $resDetails;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
