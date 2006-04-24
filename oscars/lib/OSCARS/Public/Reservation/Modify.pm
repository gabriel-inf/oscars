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

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::Library::Reservation::Pathfinder;
use OSCARS::Library::Reservation::Common;
use OSCARS::Library::Reservation::TimeConversion;

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
# soapMethod:  Handles reservation modification.  Not implemented yet.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    $results->{login} = $self->{user}->{login};
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
