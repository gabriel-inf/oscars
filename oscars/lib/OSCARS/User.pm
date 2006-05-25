# =============================================================================
package OSCARS::User;

=head1 NAME

OSCARS::User - Handles user db connections, history, authorizations.

=head1 SYNOPSIS

  use OSCARS::User;

=head1 DESCRIPTION

This module contains information about one user currently logged in.
It caches authorization information about that user, retrieved from the 
database used by the authorization plugin.  It also caches connection handles
to any other database used.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 24, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::PluginManager;
use OSCARS::Database;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    # initially not authenticated
    $self->{isAuthenticated} = 0;
    $self->{handles} = {};
    $self->{authZ} = $self->{pluginMgr}->usePlugin('authorization');
    if ( !$self->{authZ} ) {
        die( "Unable to find authorization plugin; does config file exist?");
    }
    $self->{authorizations} = $self->{authZ}->getAuthorizations($self);
} #____________________________________________________________________________


###############################################################################
# getDbHandle:  retrieves DB handle from cache for database selected, sets up 
#               connection if none exists
#
sub getDbHandle {
    my( $self, $dbname ) = @_;

    if ( !$self->{handles}->{$dbname} ) {
	$self->{handles}->{$dbname} = OSCARS::Database->new();
	$self->{handles}->{$dbname}->connect($dbname);
    }
    return $self->{handles}->{$dbname};
} #____________________________________________________________________________


###############################################################################
# closeHandles:  close all cached connections to databases
#
sub closeHandles {
    my( $self ) = @_;

    for my $db (keys(%{$self->{handles}})) {
        $self->{handles}->{$db}->disconnect();
    }
} #____________________________________________________________________________


###############################################################################
# authenticated:  returns whether user has been authenticated or not
#
sub authenticated {
    my( $self ) = @_;

    return $self->{isAuthenticated};
} #____________________________________________________________________________


###############################################################################
# setAuthenticated:  set's user authentication status
#
sub setAuthenticated {
    my( $self, $authStatus ) = @_;

    $self->{isAuthenticated} = $authStatus;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has permission to use a given resource.
#
sub authorized {
    my( $self, $resourceName, $permissionName ) = @_;

    return $self->{authZ}->authorized($self, $resourceName, $permissionName);
} #____________________________________________________________________________


###############################################################################
# getAuthorizations:  returns user's cached authorizations.
#
sub getAuthorizations {
    my( $self ) = @_;

    return $self->{authorizations};
} #____________________________________________________________________________


######
1;
