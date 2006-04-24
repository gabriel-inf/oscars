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
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 22, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Public::Institution::List;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::Public::Institution::List->new();
    $self->{profileFields} =
         'lastName, firstName, login, password, ' .
         'emailPrimary, emailSecondary, ' .
         'phonePrimary, phoneSecondary, description, ' .
#    'registerTime, activationKey, ' .
         'institutionId';
} #____________________________________________________________________________


###############################################################################
# soapMethod:  SOAP method to retrieve a user's profile.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $results = {};
    my( $statement, $results );

    my $params = $self->{params};
    # only happens if coming in from ListUsers form, which requires
    # additional authorization
    if ( $params->{selectedUser} ) {
        $statement = 'SELECT * FROM users WHERE login = ?';
        $results = $self->{db}->getRow($statement, $params->{selectedUser});
        # check whether this person is in the database
        if ( !$results ) {
            throw Error::Simple("No such user $params->{selectedUser}.");
        }
        $results->{id} = 'hidden';
    }
    else {
        $statement = "SELECT $self->{profileFields} FROM users " .
                     'WHERE login = ?';
        $results = $self->{db}->getRow($statement, $self->{user}->{login});
    }
    $results->{institutionName} = $self->{institutions}->getName(
                                   $self->{db}, $results->{institutionId});
    $results->{institutionId} = 'hidden';
    $results->{login} = $self->{user}->{login};
    $results->{selectedUser} = $params->{selectedUser};
    # X out password
    $results->{password} = 'hidden';
    $results->{institutionList} =
                $self->{institutions}->listInstitutions( $self->{db} );
    return $results;
} #____________________________________________________________________________

######
1;
