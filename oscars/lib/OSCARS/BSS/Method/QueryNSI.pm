#==============================================================================
package OSCARS::BSS::Method::QueryNSI;

=head1 NAME

OSCARS::BSS::Method::QueryNSI - SOAP method to view the details of a
specific reservation.

=head1 SYNOPSIS

  use OSCARS::BSS::Method::QueryNSI;

=head1 DESCRIPTION

SOAP method that returns the details of an OSCARS reservation, given its
database id, from the reservations table in the BSS database.
It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 12, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::BSS::ReservationCommon;
use OSCARS::BSS::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{time_methods} = OSCARS::BSS::TimeConversionCommon->new(
                                                     'user' => $self->{user});
    $self->{resv_methods} = OSCARS::BSS::ReservationCommon->new(
                                                     'user' => $self->{user});
} #____________________________________________________________________________


###############################################################################
# soap_method:  get reservation details from the database, given its
#     reservation id.  If a user has the 'manage' permission on the
#     'Reservations' resource, they can view any reservation's details.
#     Otherwise they can only view reservations that they have made, with less
#     of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub soap_method {
    my( $self ) = @_;

    my $results =
            $self->{resv_methods}->view_details($self->{params});
    $self->{time_methods}->convert_times($results);
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
