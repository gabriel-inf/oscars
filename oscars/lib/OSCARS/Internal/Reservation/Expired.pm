#==============================================================================
package OSCARS::Internal::Reservation::Expired;

=head1 NAME

OSCARS::Internal::Reservation::Expired - SOAP method to find expired 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Expired;

=head1 DESCRIPTION

SOAP method to find expired OSCARS reservations.  It calls the PSS to tear down
the label switched path for expired reservations.  Inherits from 
OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::PSS::JnxLSP;

use OSCARS::Internal::Reservation::Scheduler;
our @ISA = qw{OSCARS::Internal::Reservation::Scheduler};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{opcode} = $self->{LSP_TEARDOWN};
    $self->{opstring} = 'teardown';
} #____________________________________________________________________________


###############################################################################
#
sub getReservations {
    my ( $self, $timeInterval ) = @_;

    my $status = 'active';
    my $statement = "SELECT unix_timestamp() AS nowTime";
    my $row = $self->{db}->getRow( $statement );
    my $timeslot = $row->{nowTime} + $timeInterval;
    $statement = qq{ SELECT * FROM reservations WHERE (status = ? and
                 endTime < ?) or (status = ?)};
    return $self->{db}->doQuery($statement, $status, $timeslot, 'precancel' );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
