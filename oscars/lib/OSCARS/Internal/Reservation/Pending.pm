#==============================================================================
package OSCARS::Internal::Reservation::Pending;

=head1 NAME

OSCARS::Internal::Reservation::Pending - SOAP method to find pending 
OSCARS reservations.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Pending;

=head1 DESCRIPTION

SOAP method to find pending OSCARS reservations.  It calls the PSS to setup 
a label-switched path for pending reservations.  Inherits from OSCARS::Method.
This is one of the primary SOAP methods in OSCARS; most others are for support.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Jason Lee (jrlee@lbl.gov)

=head1 LAST MODIFIED

May 1, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Internal::Reservation::Scheduler;
our @ISA = qw{OSCARS::Internal::Reservation::Scheduler};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{opcode} = $self->{LSP_SETUP};
    $self->{opstring} = 'setup';
} #____________________________________________________________________________


###############################################################################
#
sub getReservations  { 
    my ( $self, $timeInterval ) = @_;

    my $status = 'pending';
    my $statement = "SELECT unix_timestamp() AS nowTime";
    my $row = $self->{db}->getRow( $statement );
    my $timeslot = $row->{nowTime} + $timeInterval;
    $statement = qq{ SELECT * FROM reservations WHERE status = ? and
                 startTime < ?};
    return $self->{db}->doSelect($statement, $status, $timeslot);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
