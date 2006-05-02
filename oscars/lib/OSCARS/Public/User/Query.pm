#==============================================================================
package OSCARS::Public::User::Query;

=head1 NAME

OSCARS::Public::User::Query - Retrieve one user's profile.

=head1 SYNOPSIS

  use OSCARS::Public::User::Query;

=head1 DESCRIPTION

This is a public SOAP method.  It gets user profile information from
the users table.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 27, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  SOAP method to retrieve a user's profile.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    my $user;
    my $params = $self->{params};
    my $statement = 'SELECT * FROM UserDetails WHERE login = ?';
    # only happens if coming in from UserList form, which requires
    # additional authorization
    if ( $params->{selectedUser} ) { $user = $params->{selectedUser}; }
    else { $user = $self->{user}->{login}; }
    $results = $self->{db}->getRow($statement, $user);
    # check whether this person is in the database
    if ( !$results ) {
        throw Error::Simple("No such user $user.");
    }
    else {
        $results = $self->{db}->getRow($statement, $user);
    }
    $results->{selectedUser} = $params->{selectedUser};
    $statement = 'SELECT name FROM institutions';
    $results->{institutionList} = $self->{db}->doSelect($statement);
    return $results;
} #____________________________________________________________________________


######
1;
