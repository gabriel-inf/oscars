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

May 4, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  SOAP method to retrieve a user's profile.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $response = {};
    my $user;
    my $statement = 'SELECT * FROM UserDetails WHERE login = ?';
    # only happens if coming in from UserList form, which requires
    # additional authorization
    if ( $request->{selectedUser} ) { $user = $request->{selectedUser}; }
    else { $user = $self->{user}->{login}; }
    $response = $self->{db}->getRow($statement, $user);
    # check whether this person is in the database
    if ( !$response ) {
        throw Error::Simple("No such user $user.");
    }
    $response->{selectedUser} = $request->{selectedUser};
    $statement = 'SELECT name FROM institutions';
    $response->{institutionList} = $self->{db}->doSelect($statement);
    return $response;
} #____________________________________________________________________________


######
1;
