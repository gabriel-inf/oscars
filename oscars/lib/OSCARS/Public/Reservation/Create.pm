#==============================================================================
package OSCARS::Public::Reservation::Create;

=head1 NAME

OSCARS::Public::Reservation::Create - Handles creation of circuit reservation. 

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Create;

=head1 DESCRIPTION

SOAP method to create reservation.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),
Jason Lee (jrlee@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

July 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::Reservation;
use OSCARS::Library::Topology::Pathfinder;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{reservation} = OSCARS::Library::Reservation->new(
                             'user' => $self->{user}, 'db' => $self->{db});
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles reservation creation. 
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $forwardResponse;

    $self->{pathfinder} = OSCARS::Library::Topology::Pathfinder->new(
                             'db' => $self->{db}, 'logger' => $logger );
    $logger->info("start", $request);
    # find path, and see if the next domain needs to be contacted
    my( $path, $nextDomain ) = $self->{pathfinder}->getPath( $request );
    $request->{path} = $path;     # save path for this domain
    # If nextDomain is set, forward checks to see if it is in the database,
    # and if so, forwards the request to the next domain.
    if ( $nextDomain ) {
        $request->{nextDomain} = $nextDomain;
        # TODO:  FIX (do copy here rather than in ClientForward
        $request->{ingressRouterIP} = undef;
        $request->{egressRouterIP} = undef;
        $forwardResponse =
             $self->{forwarder}->forward($request, $self->{configuration}, $logger);
    }
    # if successfuly found path, attempt to enter local domain's portion in db
    my $fields = $self->createReservation( $request );
    my $response = { 'status' => $fields->{status}, 'tag' => $fields->{tag} };
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


### Private methods. ###
 
###############################################################################
# createReservation:  builds row to insert into the reservations table,
#      checks for oversubscribed route, inserts the reservation, and
#      builds up the results to return to the client.
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub createReservation {
    my( $self, $request ) = @_;

    # Make sure no link is oversubscribed.
    $self->{reservation}->checkOversubscribed( $request );
    # Insert reservation in reservations table
    my $id = $self->{reservation}->insert( $request );
    # return status back, and tag if creation was successful
    my $statement = 'SELECT tag, status FROM ReservationUserDetails ' .
                    ' WHERE id = ?';
    return $self->{db}->getRow($statement, $id);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
