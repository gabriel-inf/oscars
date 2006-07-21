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

July 20, 2006

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

    return $self->listReservations( $request );
} #____________________________________________________________________________


###############################################################################
#
sub listReservations {
    my( $self ) = @_;

    my( $resInfoContent, $statement );

    if ( $self->{user}->authorized('Reservations', 'manage') ) {
        $statement = 'SELECT * FROM ReservationList ORDER BY startTime DESC';
        $resInfoContent = $self->{db}->doSelect($statement);
    }
    else {
        $statement = 'SELECT * FROM ReservationList WHERE login = ? ' .
                     'ORDER BY startTime DESC';
        $resInfoContent = $self->{db}->doSelect($statement, $self->{user}->{login});
    }
    # format results before returning
    my @listReply = ();
    for my $row ( @{$resInfoContent} ) {
        push( @listReply, $self->summarize($row) );
    }
    return \@listReply;
} #____________________________________________________________________________


##################
# Internal methods
##################

###############################################################################
#
sub summarize {
    my( $self, $row ) = @_;

    my $results = {};
    $results->{startTime} = $self->{reservation}->secondsToDatetime(
                              $row->{startTime}, $row->{origTimeZone});
    $results->{endTime} = $self->{reservation}->secondsToDatetime(
                              $row->{endTime}, $row->{origTimeZone});
    $results->{tag} = $row->{tag};
    $results->{status} = $row->{status};
    $results->{srcHost} = $row->{srcHost};
    $results->{destHost} = $row->{destHost};
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
