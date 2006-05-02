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

May 1, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation::TimeConversion;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new();
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles all operations for the Manage Reservations page. 
#     It uses information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    $results->{list} = $self->getReservations($self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
# getReservations:  get reservations from the database.  If the user has
#     the 'manage' permission on the 'Reservations' resource, they can view 
#     all reservations.  Otherwise they can only view their own.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub getReservations {
    my( $self, $params ) = @_;

    my( $rows, $statement );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationList ORDER BY startTime DESC';
        $rows = $self->{db}->doSelect($statement);
    }
    else {
        $statement = 'SELECT * FROM ReservationList WHERE login = ? ' .
                     'ORDER BY startTime DESC';
        $rows = $self->{db}->doSelect($statement, $self->{user}->{login});
    }
    # format results before returning
    my $results = $self->buildResults($rows);
    return $results;
} #____________________________________________________________________________


sub buildResults {
    my( $self, $rows ) = @_;

    my @results = ();
    for my $row (@$rows) {
        my $startTime = $self->{timeLib}->secondsToDatetime(
                              $row->{startTime}, $row->{origTimeZone});
        my $endTime = $self->{timeLib}->secondsToDatetime(
                              $row->{endTime}, $row->{origTimeZone});
        push(@results, { 'tag' => $row->{tag},
            'startTime' => $startTime,
            'endTime' => $endTime,
            'status' => $row->{status},
            'srcHost' => $row->{srcHost},
            'destHost' => $row->{destHost} }
        );
    }
    return \@results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
