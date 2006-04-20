#==============================================================================
package OSCARS::AAA::Method::User::Modify;

=head1 NAME

OSCARS::AAA::Method::User::Modify - Modifies a user's profile.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::User::Modify;

=head1 DESCRIPTION

This is an AAA SOAP method.  It modifies a user's profile.
It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 19, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAA::Method::Institution::List;

our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::Institution::List->new();
    $self->{profileFields} =
         'lastName, firstName, login, password, ' .
         'emailPrimary, emailSecondary, ' .
         'phonePrimary, phoneSecondary, description, ' .
#    'registerTime, activationKey, ' .
         'institutionId';
} #____________________________________________________________________________


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

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions.
    if ( $params->{institutionName} ) {
        $params->{institutionId} = $self->{institutions}->getId( 
                                    $self->{db}, $params->{institutionName} );
    }
    $results = {};    # clear any previous results
    # TODO:  allow admin to set all fields
    my @fields = split(', ', $self->{profileFields});
    $statement = 'UPDATE users SET ';
    for $_ (@fields) {
        # TODO:  allow setting field to NULL where legal
        if ( $params->{$_} ) {
            $statement .= "$_ = '$params->{$_}', ";
            # TODO:  check that query preparation correct
            $results->{$_} = $params->{$_};
	}
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE login = ?';
    my $unused = $self->{db}->doQuery($statement, $params->{login});

    $results->{selectedUser} = $params->{selectedUser};
    $results->{login} = $self->{user}->{login};
    $results->{institutionName} = $params->{institutionName};
    $results->{password} = 'hidden';
    $results->{institutionList} =
                $self->{institutions}->listInstitutions( $self->{db} );
    return $results;
} #____________________________________________________________________________


######
1;
