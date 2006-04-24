#==============================================================================
package OSCARS::Public::Reservation::Cancel;

=head1 NAME

OSCARS::Public::Reservation::Cancel - Handles cancelling reservation.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Cancel;

=head1 DESCRIPTION

SOAP method to cancel reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

April 24, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation::TimeConversion;
use OSCARS::Library::Reservation::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
                           'user' => $self->{user}, 'db' => $self->{db});
    $self->{timeLib} = OSCARS::Library::Reservation::TimeConversion->new();
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
    $results->{startTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{startTime}, $results->{origTimeZone});
    $results->{endTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{endTime}, $results->{origTimeZone});
    $results->{createdTime} = $self->{timeLib}->secondsToDatetime(
                              $results->{createdTime}, $results->{origTimeZone});
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
