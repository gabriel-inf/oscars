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

May 4, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
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
    my $status =  $self->{resvLib}->updateStatus($request->{tag}, 'precancel');
    my $response = {};
    $response->{tag} = $request->{tag};
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
