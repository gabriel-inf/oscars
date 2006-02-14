#==============================================================================
package OSCARS::User;

=head1 NAME

OSCARS::User - Handles user database connections, sessions, and history.

=head1 SYNOPSIS

  use OSCARS::User;

=head1 DESCRIPTION

This module contains information about one user currently logged in.
It caches information about that user, retrieved from the OSCARS 
database.   All operations performed against the database go through
this class, which maintains the user's database handle via the superclass.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 10, 2006

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


######
1;
