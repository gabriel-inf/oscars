#==============================================================================
package OSCARS::Intradomain::Method::ListReservations;

=head1 NAME

OSCARS::Intradomain::Method::ListReservations - Returns list of reservations. 

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::ListReservations;

=head1 DESCRIPTION

SOAP method to manage reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

April 17, 2006

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
    $self->{resv_lib} = OSCARS::Intradomain::ReservationCommon->new(
                                                 'user' => $self->{user},
                                                 'db' => $self->{db});
    $self->{time_lib} = OSCARS::Intradomain::TimeConversionCommon->new(
                                                 'db' => $self->{db},
                                                 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soap_method:  Handles all operations for the Manage Reservations page. 
#     It uses information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $results = {};
    $results->{list} = $self->get_reservations($self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_reservations:  get reservations from the database.  If the user has
#     the 'manage' permission on the 'Reservations' resource, they can view 
#     all reservations.  Otherwise they can only view their own.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub get_reservations {
    my( $self, $params ) = @_;

    my( $rows, $statement );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = "SELECT * FROM Intradomain.reservations" .
                     ' ORDER BY reservation_start_time';
        $rows = $self->{db}->do_query($statement);
    }
    else {
        $statement = 'SELECT * FROM Intradomain.reservations' .
                     ' WHERE user_login = ?' .
                     ' ORDER BY reservation_start_time';
        $rows = $self->{db}->do_query($statement, $self->{user}->{login});
    }
    for my $resv ( @$rows ) {
        $self->{time_lib}->convert_times($resv);
        $self->{resv_lib}->get_host_info($resv);
    }
    return $rows;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
