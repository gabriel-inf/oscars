#==============================================================================
package OSCARS::AAA::AuthZ;

=head1 NAME

OSCARS::AAA::AuthZ - handles authorization.

=head1 SYNOPSIS

  use OSCARS::AAA::AuthZ;

=head1 DESCRIPTION

This module handles authorization.  It queries the database to see if a user 
is authorized to perform an action.  There is one instance of this per user.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

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
} #____________________________________________________________________________


sub initialize {
    my( $self ) = @_;

    $self->{db} = OSCARS::Database->new();
    $self->{db}->connect($self->{database});
} #____________________________________________________________________________


###############################################################################
# get_resource_permissions:  Gets all permissions associated with all 
#     resources.
#
sub get_resource_permissions {
    my( $self, $user ) = @_;

    my( $row, $resource_name, $permission_name );

    my $statement = "SELECT resource_id, permission_id " .
                    "FROM resourcepermissions";
    my $results = $self->{db}->do_query($statement);
    my $resource_permissions = {};
    $statement = "SELECT resource_name FROM resources " .
                 "WHERE resource_id = ?";
    my $pstatement = "SELECT permission_name FROM permissions " .
                 "WHERE permission_id = ?";
    for my $perm ( @$results ) {
	$row = $self->{db}->get_row($statement, $perm->{resource_id});
	$resource_name = $row->{resource_name};
        if ( !$resource_permissions->{$resource_name} ) {
            $resource_permissions->{$resource_name} = {};
        }
	$row = $self->{db}->get_row($pstatement, $perm->{permission_id});
	$permission_name = $row->{permission_name};
        $resource_permissions->{$resource_name}->{$permission_name} = 1;
    }
    return $resource_permissions;
} #____________________________________________________________________________


###############################################################################
# get_authorizations:  get all authorizations for the specified user.
#
sub get_authorizations {
    my( $self, $user ) = @_;

    my( $row, $resource_name, $permission_name );

    my $auths = {};
    my $statement = "SELECT user_id from users where user_login = ?";
    my $results = $self->{db}->get_row($statement, $user->{login});
    my $user_id = $results->{user_id};

    $statement = "SELECT resource_id, permission_id FROM authorizations " .
                 "WHERE user_id = ?";
    $results = $self->{db}->do_query($statement, $user_id);
    my $rstatement = "SELECT resource_name FROM resources " .
                     "WHERE resource_id = ?";
    my $pstatement = "SELECT permission_name FROM permissions " .
                     "WHERE permission_id = ?";
    for my $pair ( @$results ) {
	$row = $self->{db}->get_row($rstatement, $pair->{resource_id});
	$resource_name = $row->{resource_name};
        if ( !$auths->{$resource_name} ) {
            $auths->{$resource_name} = {};
	}
	$row = $self->{db}->get_row($pstatement, $pair->{permission_id});
	$permission_name = $row->{permission_name};
        $auths->{$resource_name}->{$permission_name} = 1;
    }
    return $auths;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has a specific permission on a given resource.
#
sub authorized {
    my( $self, $user, $resource_name, $permission_name ) = @_;

    if ( !$user->{authorizations}->{$resource_name} ) {
	return 0;
    }
    elsif ( !$user->{authorizations}->{$resource_name}->{$permission_name} ) {
        return 0;
    }
    return 1;
} #____________________________________________________________________________


######
1;
