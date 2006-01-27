###############################################################################
package OSCARS::User;

=head1 NAME

OSCARS::User - Handles user database connections, sessions, and history.

=head1 SYNOPSIS

  use OSCARS::User;

=head1 DESCRIPTION

This module contains information about all users currently logged in.
It maintains persistent information about each user is in the OSCARS database.
It inherits from OSCARS::Database.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 20, 2005

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
    return( $self );
}

######
1;
