#==============================================================================
package OSCARS::AAA::Method::ManagePermissions;

=head1 NAME

OSCARS::AAA::Method::ManagePermissions - SOAP method to manage permissios

=head1 SYNOPSIS

  use OSCARS::AAA::Method::ManagePermissions;

=head1 DESCRIPTION

This is an AAA SOAP method.  It manages the retrieval, addition, and
delete of information from the permissions table.  The specific operation to
perform is given by the 'op' parameter. 

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 12, 2006

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
    $self->{lib} = OSCARS::AAA::ResourceLibrary->new();
    $self->{param_tests} = {};
    $self->{param_tests}->{add_permission} = {
        'permission_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's name."
            }
        ),
        'permission_description' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's description."
            }
        ),
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  Gets all information necessary for the Manage Permissions page.
#     It returns information from the permissions table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage permissions");
    }
    if (!$self->{params}->{op}) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires operation specification.");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'viewPermissions') {
        $results = $self->get_permissions($self->{user}, $self->{params});
    }
    elsif ($self->{params}->{op} eq 'addPermission') {
        $self->{lib}->add_row( $self->{user},$self->{params},'Permissions' );
        $results = $self->get_permissions($self->{user}, $self->{params});
    }
    elsif ($self->{params}->{op} eq 'deletePermission') {
        $self->delete_permission( $self->{user}, $self->{params} );
        $results = $self->get_permissions($self->{user}, $self->{params});
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_permissions:  Gets all information necessary to display the 
# Manage Permissions page.
#
# In:  reference to User instance, reference to hash of parameters
# Out: reference to hash of results
#
sub get_permissions {
    my( $self, $user, $params ) = @_;

    my $statement = "SELECT permission_name FROM permissions";
    my $results = {};
    $results->{permissions} = {};
    my $p_results = $user->do_query($statement);
    for my $row (@$p_results) {
        $results->{permissions}->{$row->{permission_name}} = 1;
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# delete_permission:  Deletes the permission with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub delete_permission {
    my( $self, $user, $params ) = @_;

    my $permission_id =
        $self->{lib}->id_from_name('permission', $params->{permission_name});
    my $statement = 'DELETE FROM permissions WHERE permission_id = ?';
    my $unused = $user->do_query($statement, $permission_id);
    my $msg = "Deleted permission named $params->{permission_name}";
    return $msg;
} #____________________________________________________________________________


######
1;
