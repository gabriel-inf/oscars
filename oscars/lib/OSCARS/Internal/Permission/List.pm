#==============================================================================
package OSCARS::Internal::Permission::List;

=head1 NAME

OSCARS::Internal::Permission::List - SOAP method to list permissios

=head1 SYNOPSIS

  use OSCARS::Internal::Permission::List;

=head1 DESCRIPTION

This is an internal SOAP method.  It returns the information in the permissions
table.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::AAA::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{lib} = OSCARS::Library::AAA::Common->new('db' => $self->{db});
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information necessary for the Manage Permissions page.
#     It returns information from the permissions table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage permissions");
    }
    my $statement = "SELECT name FROM permissions";
    my $results = {};
    $results->{permissions} = {};
    my $presults = $self->{db}->doSelect($statement);
    for my $row (@$presults) {
        $results->{permissions}->{$row->{name}} = 1;
    }
    return $results;
} #____________________________________________________________________________


######
1;
