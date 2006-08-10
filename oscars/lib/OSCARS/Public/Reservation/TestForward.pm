#==============================================================================
package OSCARS::Public::Reservation::TestForward;

=head1 NAME

OSCARS::Public::Reservation::TestForward - Handles request forwarded from BNL.

=head1 SYNOPSIS

  use OSCARS::Public::Reservation::TestForward;

=head1 DESCRIPTION

Special case SOAP method to handle a request forwarded from BNL.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

August 9, 2006

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
# soapMethod:  Handles a forwarded  request.  Makes call to forwarded method,
#              extracting that method's parameters from the payload.
#
# In:  reference to hash containing the forwarded request and its parameters, 
#      the user login, the payload sender's name and
#      OSCARS::Logger instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $payload, $logger ) = @_;
    my $forwardedRequest = $payload->{$payload->{contentType}};
    $logger->info("start", $payload);
    my $factory = OSCARS::MethodFactory->new('pluginMgr' => $self->{pluginMgr});
    my $handler =
        $factory->instantiate( $self->{user}, $payload->{contentType} );
    print STDERR "in TestForward, calling $payload->{contentType}\n";
    print STDERR "request is ", Dumper($forwardedRequest), "\n";

    my $results = $handler->soapMethod($forwardedRequest, $logger);
    print STDERR "results are", Dumper($results), "\n";
    if (!defined $results) {
	$results->{ $payload->{contentType}} = undef;
    }
    else {
        my $info = {'results' => $results};
        $logger->info("finish", $info);
    }
    return $results;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
