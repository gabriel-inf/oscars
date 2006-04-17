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

April 17, 2006

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
    $self->{is_authenticated} = 0;
    $self->{handles} = {};
    my $plugin_manager = OSCARS::PluginManager->new();
    $self->{authZ} = $plugin_manager->use_plugin('authorization');
    if ( !$self->{authZ} ) {
        die( "Unable to find authorization plugin; does config file exist?");
    }
    $self->{authorizations} = $self->{authZ}->get_authorizations($self);
} #____________________________________________________________________________


###############################################################################
# get_db_handle:  retrieves DB handle from cache for database selected, sets up 
#                 connection if none exists
#
sub get_db_handle {
    my( $self, $dbname ) = @_;

    if ( !$self->{handles}->{$dbname} ) {
	$self->{handles}->{$dbname} = OSCARS::Database->new();
	$self->{handles}->{$dbname}->connect($dbname);
    }
    return $self->{handles}->{$dbname};
} #____________________________________________________________________________


###############################################################################
# close_handles:  close all cached connections to databases
#
sub close_handles {
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

    return $self->{is_authenticated};
} #____________________________________________________________________________


###############################################################################
# set_authenticated:  set's user authentication status
#
sub set_authenticated {
    my( $self, $auth_status ) = @_;

    $self->{is_authenticated} = $auth_status;
} #____________________________________________________________________________


###############################################################################
# authorized:  See if user has permission to use a given resource.
sub authorized {
    my( $self, $resource_name, $permission_name ) = @_;

    return $self->{authZ}->authorized($self, $resource_name, $permission_name);
} #____________________________________________________________________________


###############################################################################
# get_authorizations:  returns user's cached authorizations.
sub get_authorizations {
    my( $self ) = @_;

    return $self->{authorizations};
} #____________________________________________________________________________


######
1;
