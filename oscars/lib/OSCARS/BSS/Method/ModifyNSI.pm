#==============================================================================
package OSCARS::BSS::Method::ModifyNSI;

=head1 NAME

OSCARS::BSS::Method::ModifyNSI - Handles modification of existing reservation. 

=head1 SYNOPSIS

  use OSCARS::BSS::Method::ManageReservations;

=head1 DESCRIPTION

SOAP method to modify existing reservation.  Not implemented yet.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 11, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
use OSCARS::BSS::RouteHandler;
use OSCARS::BSS::ReservationCommon;
use OSCARS::BSS::TimeConversionCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{resv_lib} = OSCARS::BSS::ReservationCommon->new(
                                                 'user' => $self->{user});
    $self->{time_lib} = OSCARS::BSS::TimeConversionCommon->new(
                                                 'user' => $self->{user},
                                                 'logger' => $self->{logger});
} #____________________________________________________________________________


###############################################################################
# soap_method:  Handles reservation modification.  Not implemented yet.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $results = {};
    $results->{user_login} = $self->{user}->{login};
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
