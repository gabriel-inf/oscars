# =============================================================================
package OSCARS::AAAS::User;

=head1 NAME

OSCARS::AAAS::User - Handles user db connections, history, authorizations.

=head1 SYNOPSIS

  use OSCARS::AAAS::User;

=head1 DESCRIPTION

This module contains information about one user currently logged in.
It caches information about that user, retrieved from the OSCARS 
database.   All operations performed against the database go through
this class, which maintains the user's database handle via the superclass.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 24, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Database;
our @ISA = qw(OSCARS::Database);

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
    $self->connect( $self->{database} );
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
# set_authorization_style:  Set current authorization style to a given package
#                           for all SOAP methods (a class method).
#
sub set_authorization_style {
    my( $self, $package_name, $database ) = @_;

    my $location = $package_name . '.pm';
    $location =~ s/(::)/\//g;
    eval { require $location };
    # overwrites any previous authorization style
    if (!$@) {
        $self->{authZ} = $package_name->new('database' => $database);
        $self->{authorizations} = $self->{authZ}->get_authorizations($self);
	return 1;
    }
    else { return 0; }
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
