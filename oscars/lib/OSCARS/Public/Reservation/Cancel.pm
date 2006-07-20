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

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                           'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles cancellation of reservation.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    $logger->info("start", $request);
    # TODO:  ensure unprivileged user can't cancel another's reservation
    my $status =  $self->{reservation}->updateStatus(
	                                        $request->{tag}, 'precancel' );
    print "cancel.pm status is $status \n";
    my $response = {};
    my $loggerInfo ={};
    $loggerInfo->{tag} = $request->{tag};
    $loggerInfo->{status} = $status;
    $logger->info("finish", $loggerInfo);
    $response->{status} = $status;
    return $response;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
