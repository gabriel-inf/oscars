#==============================================================================
package OSCARS::Library::AAA::AuthZ;

=head1 NAME

OSCARS::Library::AAA::AuthZ - handles authorization.

=head1 SYNOPSIS

  use OSCARS::Library::AAA::AuthZ;

=head1 DESCRIPTION

This module handles authorization.  It queries the database to see if a user 
is authorized to perform an action.  There is one instance of this per user.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Mary Thompson (mrthompson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

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
# getResourcePermissions:  Gets all permissions associated with all resources.
#
sub getResourcePermissions {
    my( $self ) = @_;

    my( $row, $resourceName, $permissionName );

    my $statement = "SELECT resourceId, permissionId FROM resourcePermissions";
    my $results = $self->{db}->doSelect($statement);
    my $resourcePermissions = {};
    $statement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $perm ( @$results ) {
	$row = $self->{db}->getRow($statement, $perm->{resourceId});
	$resourceName = $row->{name};
        if ( !$resourcePermissions->{$resourceName} ) {
            $resourcePermissions->{$resourceName} = {};
        }
	$row = $self->{db}->getRow($pstatement, $perm->{permissionId});
	$permissionName = $row->{name};
        $resourcePermissions->{$resourceName}->{$permissionName} = 1;
    }
    return $resourcePermissions;
} #____________________________________________________________________________


###############################################################################
# getAuthorizations:  get all authorizations for the specified user.
#
sub getAuthorizations {
    my( $self, $user ) = @_;

    my( $row, $resourceName, $permissionName );

    my $auths = {};
    my $statement = "SELECT id from users where login = ?";
    my $results = $self->{db}->getRow($statement, $user->{login});
    my $userId = $results->{id};

    $statement = "SELECT resourceId, permissionId FROM authorizations " .
                 "WHERE userId = ?";
    $results = $self->{db}->doSelect($statement, $userId);
    my $rstatement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $pair ( @$results ) {
	$row = $self->{db}->getRow($rstatement, $pair->{resourceId});
	$resourceName = $row->{name};
        if ( !$auths->{$resourceName} ) {
            $auths->{$resourceName} = {};
	}
	$row = $self->{db}->getRow($pstatement, $pair->{permissionId});
	$permissionName = $row->{name};
        $auths->{$resourceName}->{$permissionName} = 1;
    }
    return $auths;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has a specific permission on a given resource.
#
sub authorized {
    my( $self, $user, $resourceName, $permissionName ) = @_;

    if ( !$user->{authorizations}->{$resourceName} ) {
	return 0;
    }
    elsif ( !$user->{authorizations}->{$resourceName}->{$permissionName} ) {
        return 0;
    }
    return 1;
} #____________________________________________________________________________


######
1;
