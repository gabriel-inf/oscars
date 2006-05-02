#==============================================================================
package OSCARS::Internal::User::List;

=head1 NAME

OSCARS::Internal::User::List - Retrieves list of system users.

=head1 SYNOPSIS

  use OSCARS::Internal::User::List;

=head1 DESCRIPTION

SOAP method to view information about all users.  It inherits from 
OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Retrieves a list of all system users.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my( $msg );

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage users");
    }
    my $results = {};
    my $statement = "SELECT * FROM UserList";
    $results->{list} = $self->{db}->doSelect($statement);
    return $results;
} #____________________________________________________________________________


######
1;
