#==============================================================================
package OSCARS::Internal::User::Remove;

=head1 NAME

OSCARS::Internal::User::Remove - Removes a reservation system user.

=head1 SYNOPSIS

  use OSCARS::Internal::User::Remove;

=head1 DESCRIPTION

SOAP method to remove a system user.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 4, 2006

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
# soapMethod:  Removes a reservation system user.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage users");
    }
    # check to make sure user exists
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $request->{selectedUser});
    if ( !$row ) {
        throw Error::Simple("Cannot remove user $request->{selectedUser}. " .
	       	"The user does not exist.");
    }
    my $statement = 'DELETE from users where login = ?';
    $self->{db}->execStatement($statement, $request->{selectedUser});
    $statement = "SELECT * FROM UserList";
    my $response = {};
    $response->{login} = $request->{selectedUser};
    return $response;
} #____________________________________________________________________________


######
1;
