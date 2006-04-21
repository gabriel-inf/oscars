#==============================================================================
package OSCARS::Intradomain::Method::CancelReservation;

=head1 NAME

OSCARS::Intradomain::Method::CancelReservation - Handles cancelling reservation.

=head1 SYNOPSIS

  use OSCARS::Intradomain::Method::CancelReservation;

=head1 DESCRIPTION

SOAP method to cancel reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

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
    $self->{resvLib} = OSCARS::Intradomain::ReservationCommon->new(
                           'user' => $self->{user}, 'db' => $self->{db});
    $self->{timeLib} = OSCARS::Intradomain::TimeConversionCommon->new(
                           'db' => $self->{db}, 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles cancellation of reservation.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    # TODO:  ensure unprivileged user can't cancel another's reservation
    my $status =  $self->{resvLib}->updateStatus(
                          $self->{params}->{id}, 'precancel' );
    my $results = $self->{resvLib}->listDetails($self->{params});
    $results->{id} = $self->{params}->{id};
    $self->{timeLib}->convertTimes($results);
    $self->{logger}->info("finish", $results);
    return $results;
} #____________________________________________________________________________


###############################################################################
# generateMessage:  generate cancelled email message
#
sub generateMessage {
    my( $self, $resv ) = @_;

    my( @messages );
    my $login = $self->{user}->{login};
    my $msg = "Reservation cancelled by $login with parameters:\n";
    $msg .= $self->{resvLib}->reservationStats($resv);
    my $subject = "Reservation cancelled by $login.";
    push(@messages, { 'msg' => $msg, 'subject' => $subject, 'user' => $login } ); 
    return( \@messages );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
