#==============================================================================
package OSCARS::Public::User::Logout;

=head1 NAME

OSCARS::Public::User::Logout - SOAP method for logout.

=head1 SYNOPSIS

  use OSCARS::Public::User::Logout;

=head1 DESCRIPTION

SOAP method for logout.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 4, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  Logout.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $response = {};
    $response->{login} = $self->{user}->{login};
    $logger->info("successful", $response);
    return $response;
} #____________________________________________________________________________


######
1;
