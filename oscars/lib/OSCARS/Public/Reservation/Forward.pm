#==============================================================================
package OSCARS::Public::Reservation::Forward;

=head1 NAME

OSCARS::Public::Reservation::Forward - Handles request forwarded from other domain.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::Forward;

=head1 DESCRIPTION

SOAP method to handle a request forwarded from another domain (currently only
OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

May 4, 2006

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
# In:  reference to hash containing a request and its parameters, and 
#      OSCARS::Logger instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $payload, $logger ) = @_;

    my $forwardedRequest = $payload->{request};
    $logger->info("start", $payload);
    my $factory = OSCARS::MethodFactory->new();
    my $handler =
        $factory->instantiate( 'user' => $self->{user},
                               'method' => $forwardedRequest->{method} );
    my $response = $handler->soapMethod($forwardedRequest, $logger);
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
