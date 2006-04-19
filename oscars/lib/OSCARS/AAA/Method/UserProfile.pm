#==============================================================================
package OSCARS::AAA::Method::UserProfile;

=head1 NAME

OSCARS::AAA::Method::UserProfile - Handles unprivileged user's profile.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::UserProfile;

=head1 DESCRIPTION

This is an AAA SOAP method.  It gets user profile information from
the users table.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAA::Method::ManageInstitutions;

our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::ManageInstitutions->new();
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

    if ( !$self->{params}->{op} ) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires specification of an operation");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'queryProfile') {
        $results = $self->queryProfile( $self->{params} ); }
    elsif ($self->{params}->{op} eq 'modifyProfile') {
        $results = $self->modifyProfile( $self->{params} );
    }
    $results->{institutionList} =
                $self->{institutions}->queryInstitutions( $self->{db} );
    return $results;
} #____________________________________________________________________________


###############################################################################
# queryProfile:  Gets the user profile for a particular user.  If the
#     user is coming in from the ManageProfile form, she can get more detailed 
#     information for herself or another user.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub queryProfile {
    my( $self, $params ) = @_;

    my( $statement, $results );

    # only happens if coming in from ManageProfile form, which requires
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
    return $results;
} #____________________________________________________________________________


###############################################################################
# modifyProfile:  Modifies the user profile for a particular user.  If the
#     user is coming in via the ManageProfile form, she can set the 
#     information for another user.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub modifyProfile {
    my( $self, $params ) = @_;

    my( $statement, $results );

    # only happens if coming in from ManageProfile form, which requires
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
    return $results;
} #____________________________________________________________________________


######
1;
