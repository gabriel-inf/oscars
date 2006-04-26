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

April 26, 2006

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
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my( $statement, $results );

    my $params = $self->{params};
    # only happens if coming in from ListUsers form, which requires
    # additional authorization
    if ( $params->{selectedUser} ) {
        # check whether this person is in the database
        $statement = 'SELECT login FROM users WHERE login = ?';
        $results = $self->{db}->getRow($statement, $params->{selectedUser});
        if ( !$results ) {
            throw Error::Simple("No such user $params->{selectedUser}.");
        }
    }
    $params->{login} = $self->{user}->{login};

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $params->{passwordNewOnce} ) {
        $params->{password} = crypt( $params->{passwordNewOnce}, 'oscars');
    }

    my $statement = 'SELECT id FROM institutions WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $params->{institutionName});
    $params->{institutionId} = $row->{id};

    # TODO:  FIX way to get update fields
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->doQuery( $statement );

    $statement = 'UPDATE users SET ';
    for $_ (@$rows) {
        # TODO:  allow setting field to NULL where legal
        if ( $params->{$_->{Field}} ) {
            $statement .= "$_->{Field} = '$params->{$_->{Field}}', ";
            # TODO:  check that query preparation correct
            $results->{$_->{Field}} = $params->{$_->{Field}};
	}
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE login = ?';
    my $unused = $self->{db}->doQuery($statement, $params->{selectedUser});
    $results->{selectedUser} = $params->{selectedUser};
    $results->{institutionName} = $params->{institutionName};
    return $results;
} #____________________________________________________________________________


######
1;
