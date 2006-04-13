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
    $self->{param_tests}->{addAuthorization} = {
        'permission_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission name."
            }
        ),
        'resource_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource name."
            }
        ),
        'user_login' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
    }
} #____________________________________________________________________________


###############################################################################
# soap_method:  Gets all information for the Manage Authorizations page. 
#     It returns information from the resources, permissions, and
#     authorizations tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
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
    if ($self->{params}->{op} eq 'viewAuthorizations') {
        $results = $self->get_authorizations($self->{user}, $self->{params});
    }
    elsif ($self->{params}->{op} eq 'addAuthorization') {
        $self->{lib}->add_row( $self->{user}, $self->{params},
                               'Authorizations' );
        $results = $self->get_authorizations($self->{user}, $self->{params});
    }
    elsif ($self->{params}->{op} eq 'deleteAuthorization') {
        $self->delete_reservation( $self->{user}, $self->{params} );
        $results = $self->get_authorizations($self->{user}, $self->{params});
    }
    elsif ($self->{params}->{op} eq 'selectUser') {
	;
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_authorizations:   Returns all information from the resourcepermissions
#     and authorizations tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub get_authorizations {
    my( $self, $user, $params ) = @_;

    my $results = {};
    my $statement = "SELECT user_login FROM users";
    $results->{users} = {};
    my $aux_results = $user->do_query($statement);
    for my $row (@$aux_results) { $results->{users}->{$row->{user_login}} = 1; }

    $results->{resource_permissions} = $self->{lib}->get_resource_permissions(
	                                       $self->{user}, $self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
# delete_authorization:  Deletes authorization given the input parameters.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub delete_authorization {
    my( $self, $user, $params ) = @_;

    my $statement = 'SELECT user_id FROM users WHERE user_login = ?';
    my $row = $user->get_row($statement, $params->{user_login});
    my $user_id = $row->{user_id};
    $statement = 'SELECT resource_id FROM resources WHERE resource_name = ?';
    $row = $user->get_row($statement, $params->{resource_name});
    my $resource_id = $row->{resource_id};
    $statement = 'SELECT permission_id FROM permissions ' .
                 'WHERE permission_name = ?';
    $row = $user->get_row($statement, $params->{permission_name});
    my $permission_id = $row->{permission_id};
    $statement = 'DELETE FROM authorizations WHERE user_id = ? AND ' .
                 'resource_id = ? AND permission_id = ?';
    my $unused = $user->do_query($statement, $user_id, $resource_id,
                                 $permission_id);
    my $msg = "Deleted authorization for $params->{user_login} involving " .
              " resource $params->{resource_name} and " .
              "permission $params->{permission_name}";
    return $msg;
} #____________________________________________________________________________


######
1;
