#==============================================================================
package OSCARS::AAA::Method::ManageResources;

=head1 NAME

OSCARS::AAA::Method::ManageResources - SOAP method to manage resources

=head1 SYNOPSIS

  use OSCARS::AAA::Method::ManageResources;

=head1 DESCRIPTION

This is an AAA SOAP method.  It manages the retrieval of information from
the permissions, resources, and resourcepermissions tables, as well
as additions and deletions upon the latter two tables.  The specific 
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
    $self->{paramTests}->{addResource} = {
        'name' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource's name."
            }
        ),
        'description' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource's description."
            }
        ),
    };
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information necessary for the Manage Resources page. 
#     It returns information from the resources and permissions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage resources");
    }
    if ( !$self->{params}->{op} ) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires specification of an operation");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'listResources') {
        $results = $self->getResourceTables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addResource') {
        $self->{lib}->addRow( $self->{params}, 'Resources' );
        $results = $self->getResourceTables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addResourcePermission') {
        $self->addRow($self->{params}, 'ResourcePermissions');
        $results = $self->getResourceTables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'deleteResourcePermission') {
        $self->deleteResourcePermission($self->{params} );
        $results = $self->getResourceTables($self->{params});
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# getResourceTables:   Returns information from the resources, permissions, 
# and resourcepermissions tables.
#
# In:  reference to User instance, reference to hash of parameters
# Out: reference to hash of results
#
sub getResourceTables {
    my( $self, $params ) = @_;

    my( $resourceName, $permissionName );

    my $results = {};
    my $statement = "SELECT name FROM resources";
    $results->{resources} = {};
    my $rresults = $self->{db}->doQuery($statement);
    for my $row (@$rresults) {
        $results->{resources}->{$row->{name}} = 1;
    }

    my $statement = "SELECT name FROM permissions";
    $results->{permissions} = {};
    my $presults = $self->{db}->doQuery($statement);
    for my $row (@$presults) {
        $results->{permissions}->{$row->{name}} = 1;
    }

    $results->{resourcePermissions} =
                       $self->{lib}->getResourcePermissions($params);
    return $results;
} #____________________________________________________________________________


###############################################################################
# deleteResource:  Deletes resource with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub deleteResource {
    my( $self, $params ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $params->{resourceName});
    my $statement = 'DELETE FROM resources WHERE id = ?';
    my $unused = $self->{db}->doQuery($statement, $resourceId);
    my $msg = "Deleted resource with name $params->{resourceName}";
    return $msg;
} #____________________________________________________________________________


###############################################################################
# deleteResourcePermission:  Deletes a resource/permission pair.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub deleteResourcePermission {
    my( $self, $params ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $params->{resourceName});
    my $permissionId =
        $self->{lib}->idFromName('permission', $params->{permissionName});
    my $statement = 'DELETE FROM resourcePermissions WHERE resourceId = ? ' .
                    'AND permissionId = ?';
    my $unused = $self->{db}->doQuery($statement, $resourceId, $permissionId);
    my $msg = "Deleted resource permission pair";
    return $msg;
} #____________________________________________________________________________


######
1;
