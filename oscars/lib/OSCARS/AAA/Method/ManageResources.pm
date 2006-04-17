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
    $self->{param_tests} = {};
    $self->{param_tests}->{add_resource} = {
        'resource_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource's name."
            }
        ),
        'resource_description' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource's description."
            }
        ),
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  Gets all information necessary for the Manage Resources page. 
#     It returns information from the resources and permissions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
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
    if ($self->{params}->{op} eq 'viewResources') {
        $results = $self->get_resource_tables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addResource') {
        $self->{lib}->add_row( $self->{params}, 'Resources' );
        $results = $self->get_resource_tables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addResourcePermission') {
        $self->add_row($self->{params}, 'ResourcePermissions');
        $results = $self->get_resource_tables($self->{params});
    }
    elsif ($self->{params}->{op} eq 'deleteResourcePermission') {
        $self->delete_resource_permission($self->{params} );
        $results = $self->get_resource_tables($self->{params});
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_resource_tables:   Returns information from the resources, permissions, 
# and resourcepermissions tables.
#
# In:  reference to User instance, reference to hash of parameters
# Out: reference to hash of results
#
sub get_resource_tables {
    my( $self, $params ) = @_;

    my( $resource_name, $permission_name );

    my $results = {};
    my $statement = "SELECT resource_name FROM resources";
    $results->{resources} = {};
    my $r_results = $self->{db}->do_query($statement);
    for my $row (@$r_results) {
        $results->{resources}->{$row->{resource_name}} = 1;
    }

    my $statement = "SELECT permission_name FROM permissions";
    $results->{permissions} = {};
    my $p_results = $self->{db}->do_query($statement);
    for my $row (@$p_results) {
        $results->{permissions}->{$row->{permission_name}} = 1;
    }

    $results->{resource_permissions} =
                       $self->{lib}->get_resource_permissions($params);
    return $results;
} #____________________________________________________________________________


###############################################################################
# delete_resource:  Deletes resource with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub delete_resource {
    my( $self, $params ) = @_;

    my $resource_id = $self->{lib}->id_from_name('resource',
                                                 $params->{resource_name});
    my $statement = 'DELETE FROM resources WHERE resource_id = ?';
    my $unused = $self->{db}->do_query($statement, $resource_id);
    my $msg = "Deleted resource with name $params->{resource_name}";
    return $msg;
} #____________________________________________________________________________


###############################################################################
# delete_resource_permission:  Deletes a resource/permission pair.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub delete_resource_permission {
    my( $self, $params ) = @_;

    my $resource_id = $self->{lib}->id_from_name('resource',
                                                 $params->{resource_name});
    my $permission_id =
        $self->{lib}->id_from_name('permission', $params->{permission_name});
    my $statement = 'DELETE FROM resourcepermissions WHERE resource_id = ?' .
                    ' AND permission_id = ?';
    my $unused = $self->{db}->do_query($statement, $resource_id, $permission_id);
    my $msg = "Deleted resource permission pair";
    return $msg;
} #____________________________________________________________________________


######
1;
