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
    $self->{paramTests}->{addPermission} = {
        'name' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's name."
            }
        ),
        'description' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's description."
            }
        ),
    };
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
    if (!$self->{params}->{op}) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires operation specification.");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'listPermissions') {
        $results = $self->getPermissions($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addPermission') {
        $self->{lib}->addRow( $self->{params},'Permissions' );
        $results = $self->getPermissions($self->{params});
    }
    elsif ($self->{params}->{op} eq 'deletePermission') {
        $self->deletePermission( $self->{params} );
        $results = $self->getPermissions($self->{params});
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# getPermissions:  Gets all information necessary to display the 
# Manage Permissions page.
#
# In:  reference to User instance, reference to hash of parameters
# Out: reference to hash of results
#
sub getPermissions {
    my( $self, $params ) = @_;

    my $statement = "SELECT name FROM permissions";
    my $results = {};
    $results->{permissions} = {};
    my $presults = $self->{db}->doQuery($statement);
    for my $row (@$presults) {
        $results->{permissions}->{$row->{name}} = 1;
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# deletePermission:  Deletes the permission with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub deletePermission {
    my( $self, $params ) = @_;

    my $permissionId =
        $self->{lib}->idFromName('permission', $params->{permissionName});
    my $statement = 'DELETE FROM permissions WHERE id = ?';
    my $unused = $self->{db}->doQuery($statement, $permissionId);
    my $msg = "Deleted permission named $params->{permissionName}";
    return $msg;
} #____________________________________________________________________________


######
1;
