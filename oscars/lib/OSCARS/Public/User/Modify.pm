#==============================================================================
package OSCARS::Public::User::Modify;

=head1 NAME

OSCARS::Public::User::Modify - Modifies a user's profile.

=head1 SYNOPSIS

  use OSCARS::Public::User::Modify;

=head1 DESCRIPTION

This is an public SOAP method.  It modifies a user's profile.  It inherits 
from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

August 9, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soapMethod:  SOAP method performing requested operation on a user's profile.
#     The default operation is to get the user's profile.  This method accesses
#     the users and institutions tables.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my( $statement, $response );

    # only happens if coming in from ListUsers form, which requires
    # additional authorization
    if ( $request->{selectedUser} ) {
        # check whether this person is in the database
        $statement = 'SELECT login FROM users WHERE login = ?';
        $response = $self->{db}->getRow($statement, $request->{selectedUser});
        if ( !$response ) {
            throw Error::Simple("No such user $request->{selectedUser}.");
        }
    }
    else { $request->{selectedUser} = $self->{user}->{login}; }
    # don't want to set this field in table
    $request->{login} = undef;

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $request->{passwordNewOnce} ) {
        $request->{password} = crypt( $request->{passwordNewOnce}, 'oscars');
    }

    my $statement = 'SELECT id FROM institutions WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $request->{institutionName});
    $request->{institutionId} = $row->{id};

    # TODO:  FIX way to get update fields
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->doSelect( $statement );

    $statement = 'UPDATE users SET ';
    for $_ (@$rows) {
        # TODO:  allow setting field to NULL where legal
        if ( $request->{$_->{Field}} ) {
            $statement .= "$_->{Field} = '$request->{$_->{Field}}', ";
            # TODO:  check that query preparation correct
            $response->{$_->{Field}} = $request->{$_->{Field}};
	}
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE login = ?';
    $self->{db}->execStatement($statement, $request->{selectedUser});
    $response->{selectedUser} = $request->{selectedUser};
    $response->{institutionName} = $request->{institutionName};
    $statement = 'SELECT name FROM institutions';
    $response->{institutionList} = $self->{db}->doSelect($statement);
    return $response;
} #____________________________________________________________________________


######
1;
