#==============================================================================
package OSCARS::Public::Reservation::Forward;

=head1 NAME

OSCARS::Public::Reservation::Forward - Forward a request to another domain.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Forward;

=head1 DESCRIPTION

SOAP method to forward a request to another domain (currently only
OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles forwarding a request.  Makes call to forwarded method,
#              extracting that method's parameters from the payload.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $payload = $self->{params};
    my $params = $payload->{params};
    $self->{logger}->info("start", $self->{params});
    my $factory = OSCARS::MethodFactory->new();
    my $handler =
        $factory->instantiate( $self->{user}, $params, $self->{logger} );
    my $results = $handler->soapMethod();
    $self->{logger}->info("finish", $self->{params});
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
