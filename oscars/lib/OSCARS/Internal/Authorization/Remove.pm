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

May 4, 2006

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
# soapMethod:  Deletes a row containing an authorization triple.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage authorizations");
    }
    my $response = {};
    my $statement = 'SELECT id FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $request->{login});
    my $userId = $row->{id};
    $statement = 'SELECT id FROM resources WHERE name = ?';
    $row = $self->{db}->getRow($statement, $request->{resourceName});
    my $resourceId = $row->{id};
    $statement = 'SELECT id FROM permissions WHERE name = ?';
    $row = $self->{db}->getRow($statement, $request->{permissionName});
    my $permissionId = $row->{id};
    $statement = 'DELETE FROM authorizations WHERE userId = ? AND ' .
                 'resourceId = ? AND permissionId = ?';
    $self->{db}->execStatement($statement, $userId, $resourceId, $permissionId);
    return $response;
} #____________________________________________________________________________


######
1;
