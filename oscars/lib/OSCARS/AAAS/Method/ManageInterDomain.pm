#==============================================================================
package OSCARS::AAAS::Method::ManageInterDomain;

=head1 NAME

OSCARS::AAAS::Method::ManageInterDomain - Manages inter-domain trust setup.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ManageInterDomain;

=head1 DESCRIPTION

This is an AAAS SOAP method.  It manages inter-domain setup and communications.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 10, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

###############################################################################
# soap_method:  Gets all information necessary for the Manage InterDomain page. 
#     It returns information from the permissions table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $results =
        $self->get_inter_domain($self->{user}, $self->{params});
    $self->{logger}->add_string("Inter-domain setup page");
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_inter_domain:  Stub at the moment.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub get_inter_domain {
    my( $self, $user, $params ) = @_;

    my $results = {};
    return $results;
} #____________________________________________________________________________


######
1;
