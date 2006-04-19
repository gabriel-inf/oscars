#==============================================================================
package OSCARS::AAA::Method::Logout;

=head1 NAME

OSCARS::AAA::Method::Logout - SOAP method for logout.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Logout;

=head1 DESCRIPTION

SOAP method for logout.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  Logout.
# In:  reference to hash of parameters
# Out: reference to hash of results of user logout.
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    $results->{login} = $self->{user}->{login};
    $self->{logger}->info("successful", $results);
    return $results;
} #____________________________________________________________________________


######
1;
