#==============================================================================
package OSCARS::AAAS::AuthZ;

=head1 NAME

OSCARS::AAAS::AuthZ - handles authorization for OSCARS.

=head1 SYNOPSIS

  use OSCARS::AAAS::AuthZ;

=head1 DESCRIPTION

This module handles OSCARS authorization.  It queries the database to
see if a user is authorized to perform an action.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 15, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::Database;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    bless($self, $class);
    $self->initialize();
    return($self);
}

sub initialize {
    my( $self ) = @_;

    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    $self->set_permissions($dbconn);
    $self->set_authorizations($dbconn);
    $dbconn->disconnect();
} #____________________________________________________________________________


###############################################################################
# set_permissions:  Gets all permissions associated with all resources, and
#                   caches them.
#
sub set_permissions {
    my( $self, $dbconn ) = @_;

    my( $row, $resource_name, $permission_name );

    my $statement = "SELECT resource_id, permission_id " .
                    "FROM resourcepermissions";
    my $results = $dbconn->do_query($statement);
    my $resource_permissions = {};
    $statement = "SELECT resource_name FROM resources " .
                 "WHERE resource_id = ?";
    my $pstatement = "SELECT permission_name FROM permissions " .
                 "WHERE permission_id = ?";
    for my $perm ( @$results ) {
	$row = $dbconn->get_row($statement, $perm->{resource_id});
	$resource_name = $row->{resource_name};
        if ( !$resource_permissions->{$resource_name} ) {
            $resource_permissions->{$resource_name} = {};
        }
	$row = $dbconn->get_row($pstatement, $perm->{permission_id});
	$permission_name = $row->{permission_name};
        $resource_permissions->{$resource_name}->{$permission_name} = 1;
    }
    $self->{resource_permissions} = $resource_permissions;
} #____________________________________________________________________________


###############################################################################
# set_authorizations:  gets all authorizations from database, and caches them.
#
sub set_authorizations {
    my( $self, $dbconn ) = @_;

    my( $row, $user_dn, $resource_name, $permission_name );

    my $auths = {};
    my $statement = "SELECT user_id, resource_id, permission_id " .
                    "FROM authorizations";
    my $results = $dbconn->do_query($statement);
    my $ustatement = "SELECT user_dn from users where user_id = ?";
    my $rstatement = "SELECT resource_name FROM resources " .
                     "WHERE resource_id = ?";
    my $pstatement = "SELECT permission_name FROM permissions " .
                     "WHERE permission_id = ?";
    for my $auth ( @$results ) {
	$row = $dbconn->get_row($ustatement, $auth->{user_id});
	$user_dn = $row->{user_dn};
        if ( !$auths->{$user_dn} ) {
            $auths->{$user_dn} = {};
        }
	$row = $dbconn->get_row($rstatement, $auth->{resource_id});
	$resource_name = $row->{resource_name};
        if ( !$auths->{$user_dn}->{$resource_name} ) {
            $auths->{$user_dn}->{$resource_name} = {};
	}
	$row = $dbconn->get_row($pstatement, $auth->{permission_id});
	$permission_name = $row->{permission_name};
        $auths->{$user_dn}->{$resource_name}->{$permission_name} = 1;
    }
    $self->{authorizations} = $auths;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has authorization to use a given resource.
sub authorized {
    my( $self, $user, $params ) = @_;

    my $resource_name = $params->{method};
    my $user_dn = $user->{dn};
    if ( !$self->{authorizations}->{$user_dn}->{$resource_name} ||
         !$self->{resource_permissions}->{$resource_name} ) {
            throw Error::Simple(
                "User $user_dn not authorized to use $resource_name");
    }
    my $permissions_necessary =
            $self->{resource_permissions}->{$resource_name};
    my $permissions_obtained =
            $self->{authorizations}->{$user_dn}->{$resource_name};
    # user must have all necessary permissions
    for my $testperm (keys %{$permissions_necessary} ) {
	if ( !$permissions_obtained->{$testperm} ) {
            throw Error::Simple(
                "User $user_dn not authorized to use $resource_name");
        }
    } 
    return $self->get_authorizations($user_dn);
} #____________________________________________________________________________


###############################################################################
# get_authorizations:  Get all authorizations, or just associations
#    associated with a particular user.
#
sub get_authorizations {
    my( $self, $selected_user ) = @_;

    if ( $selected_user ) {
        return $self->{authorizations}->{$selected_user};
    }
    return $self->{authorizations};
} #____________________________________________________________________________
	   

######
1;
