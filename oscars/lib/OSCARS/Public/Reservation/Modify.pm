#==============================================================================
package OSCARS::Public::Reservation::Modify;

=head1 NAME

OSCARS::Public::Reservation::Modify- Handles modification of existing reservation. 

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Modify;

=head1 DESCRIPTION

SOAP method to modify existing reservation.  Not implemented yet.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Topology::Pathfinder;
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
# soapMethod:  Handles reservation modification.  Not implemented yet.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $response = {};
    $response->{login} = $self->{user}->{login};
    return $response;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
