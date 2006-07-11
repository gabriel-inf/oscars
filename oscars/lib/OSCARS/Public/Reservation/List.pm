#==============================================================================
package OSCARS::Public::Reservation::List;

=head1 NAME

OSCARS::Public::Reservation::List - Returns list of reservations. 

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::List;

=head1 DESCRIPTION

SOAP method to manage reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

July 10, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

use OSCARS::Library::Reservation;

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                           'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Retrieves a list of all reservations from the database. 
#     If the user has
#     the 'manage' permission on the 'Reservations' resource, they can view 
#     all reservations.  Otherwise they can only view their own.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to array of hashes
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    return $self->{reservation}->listReservationsResponse( $request );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
