#==============================================================================
package OSCARS::Internal::Authorization::Remove;

=head1 NAME

OSCARS::Internal::Authorization::Remove - Remove an authorization

=head1 SYNOPSIS

  use OSCARS::Internal::Authorization::Remove;

=head1 DESCRIPTION

This is an internal SOAP method.  It manages the deletion of a row from the
authorizations table.

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
# soapMethod:  Deletes an authorization triple.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage authorizations");
    }
    my $results = {};
    my $statement = 'SELECT id FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $self->{params}->{login});
    my $userId = $row->{id};
    $statement = 'SELECT id FROM resources WHERE name = ?';
    $row = $self->{db}->getRow($statement, $self->{params}->{resourceName});
    my $resourceId = $row->{id};
    $statement = 'SELECT id FROM permissions WHERE name = ?';
    $row = $self->{db}->getRow($statement, $self->{params}->{permissionName});
    my $permissionId = $row->{id};
    $statement = 'DELETE FROM authorizations WHERE userId = ? AND ' .
                 'resourceId = ? AND permissionId = ?';
    my $unused =
        $self->{db}->doQuery($statement, $userId, $resourceId, $permissionId);
    my $msg = "Removed authorization for $self->{params}->{login} involving " .
              "resource $self->{params}->{resourceName} and " .
              "permission $self->{params}->{permissionName}";
    return $results;
} #____________________________________________________________________________


######
1;
