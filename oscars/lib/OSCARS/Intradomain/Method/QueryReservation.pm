#==============================================================================
package OSCARS::Intradomain::Method::QueryReservation;

=head1 NAME

OSCARS::Intradomain::Method::QueryReservation - SOAP method to list the details of a
specific reservation.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::QueryReservation;

=head1 DESCRIPTION

SOAP method that returns the details of a reservation, given its
database id, from the reservations table in the Intradomain database.
It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 19, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Intradomain::ReservationCommon;
use OSCARS::Intradomain::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{timeLib} = OSCARS::Intradomain::TimeConversionCommon->new(
                                 'db' => $self->{db});
    $self->{resvLib} = OSCARS::Intradomain::ReservationCommon->new(
                                 'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  get reservation details from the database, given its
#     reservation id.  If a user has the 'manage' permission on the
#     'Reservations' resource, they can list any reservation's details.
#     Otherwise they can only list reservations that they have made, with less
#     of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub soapMethod {
    my( $self ) = @_;

    my $results = $self->{resvLib}->listDetails($self->{params});
    $self->{timeLib}->convertTimes($results);
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
