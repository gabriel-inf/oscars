#==============================================================================
package OSCARS::AAA::Method::ManageAuthorizations;

=head1 NAME

OSCARS::AAA::Method::ManageAuthorizations - Handles authorizations

=head1 SYNOPSIS

  use OSCARS::AAA::Method::ManageAuthorizations;

=head1 DESCRIPTION

This is an AAA SOAP method.  It manages the retrieval of information from
the permissions, resources, resourcepermissions and authorizations tables, as 
well as additions and deletions upon the authoriozations table,  The specific 
operation to perform is given by the 'op' parameter. 

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAA::ResourceLibrary;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{lib} = OSCARS::AAA::ResourceLibrary->new('db' => $self->{db});
    $self->{paramTests} = {};
    $self->{paramTests}->{addAuthorization} = {
        'permissionName' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission name."
            }
        ),
        'resourceName' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource name."
            }
        ),
        'login' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
    }
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information for the Manage Authorizations page. 
#     It returns information from the resources, permissions, and
#     authorizations tables.
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
    if (!$self->{params}->{op}) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires operation to be specified");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'listAuthorizations') {
        $results = $self->getAuthorizations($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addAuthorization') {
        $self->{lib}->addRow( $self->{params}, 'Authorizations' );
        $results = $self->getAuthorizations($self->{params});
    }
    elsif ($self->{params}->{op} eq 'deleteAuthorization') {
        $self->deleteReservation( $self->{params} );
        $results = $self->getAuthorizations($self->{params});
    }
    elsif ($self->{params}->{op} eq 'selectUser') {
	;
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# getAuthorizations:   Returns all information from the resourcepermissions
#     and authorizations tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub getAuthorizations {
    my( $self, $params ) = @_;

    my $results = {};
    my $statement = "SELECT login FROM users";
    $results->{users} = {};
    my $auxResults = $self->{db}->doQuery($statement);
    for my $row (@$auxResults) { $results->{users}->{$row->{login}} = 1; }

    $results->{resourcePermissions} =
        $self->{lib}->getResourcePermissions( $self->{params} );
    return $results;
} #____________________________________________________________________________


###############################################################################
# deleteAuthorization:  Deletes authorization given the input parameters.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub deleteAuthorization {
    my( $self, $params ) = @_;

    my $statement = 'SELECT id FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $params->{login});
    my $userId = $row->{id};
    $statement = 'SELECT id FROM resources WHERE name = ?';
    $row = $self->{db}->getRow($statement, $params->{resourceName});
    my $resourceId = $row->{id};
    $statement = 'SELECT id FROM permissions WHERE name = ?';
    $row = $self->{db}->getRow($statement, $params->{permissionName});
    my $permissionId = $row->{id};
    $statement = 'DELETE FROM authorizations WHERE userId = ? AND ' .
                 'resourceId = ? AND permissionId = ?';
    my $unused =
        $self->{db}->doQuery($statement, $userId, $resourceId, $permissionId);
    my $msg = "Deleted authorization for $params->{login} involving " .
              "resource $params->{resourceName} and " .
              "permission $params->{permissionName}";
    return $msg;
} #____________________________________________________________________________


######
1;
