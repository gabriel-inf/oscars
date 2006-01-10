#==============================================================================
package OSCARS::AAAS::Method::ViewPermissions;

=head1 NAME

OSCARS::AAAS::Method::ViewPermissions - Returns list of permissions.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ViewPermissions;

=head1 DESCRIPTION

SOAP method for viewing permissions required for OSCARS methods.  Only callable
by users with administrative privileges.  It inherits from OSCARS::Method.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 9, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soap_method:  Retrieves information for all permissions (used in
#     GetProfile, SetProfile, and AddUser).
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $statement = 'SELECT user_level_description from user_levels';
    my $results = $self->{user}->do_query($statement);
    return $results;
} #____________________________________________________________________________


######
1;
