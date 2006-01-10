#==============================================================================
package OSCARS::AAAS::Method::ViewInstitutions;

=head1 NAME

OSCARS::AAAS::Method::ViewInstitutions - SOAP method to view institutions.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ViewInstitutions;

=head1 DESCRIPTION

SOAP method returning the list of institutions with users participating in
OSCARS.  It inherits from OSCARS::Method.

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
# soap_method:  Retrieves information for all institutions (used in
#     GetProfile, SetProfile, and AddUser).
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $statement = 'SELECT institution_name from institutions';
    my $results = $self->{user}->do_query($statement);
    return $results;

} #____________________________________________________________________________


######
1;
