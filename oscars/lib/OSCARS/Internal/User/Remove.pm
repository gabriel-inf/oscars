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

April 20, 2006

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
    my $params = $self->{params};
    # check to make sure user exists
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $params->{selectedUser});
    if ( !$row ) {
        throw Error::Simple("Cannot remove user $params->{selectedUser}. " .
	       	"The user does not exist.");
    }
    my $statement = 'DELETE from users where login = ?';
    my $unused = $self->{db}->doQuery($statement, $params->{selectedUser});
    $statement = "SELECT * FROM userList";
    $results->{list} = $self->{db}->doQuery($statement);
    return $results;
} #____________________________________________________________________________


######
1;
