#==============================================================================
package OSCARS::AAA::Method::User::Activate;

=head1 NAME

OSCARS::AAA::Method::User::Activate - Currently a noop.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::User::Activate;

=head1 DESCRIPTION

SOAP method to activate a user.  Currently a noop.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 19, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::AAA::Method::Institution::List;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::Institution::List->new();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information necessary for the Manage Users page. 
#     It returns information from the users and institutions tables.
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
    return $results;
} #____________________________________________________________________________


###############################################################################
# activateAccount:  Activate a user's account.  Not functional.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub activateAccount {
    my( $self, $params ) = @_;

    my ( $results) ;

    my $login = $self->{user}->{login};
    # get the password from the database
    my $statement = 'SELECT password, activationKey ' .
                    'FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);

    # check whether this person is a registered user
    if ( !$row ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    my $keysMatch = 0;
    my( $pendingLevel, $nonMatchError );
        # this login name is in the database; compare passwords
    if ( $row->{activationKey} eq '' ) {
        $nonMatchError = 'This account has already been activated.';
    }
    elsif ( $row->{password} ne $params->{password} ) {
        $nonMatchError = 'Please check your password and try again.';
    }
    elsif ( $row->{activationKey} ne $params->{activationKey} ) {
        $nonMatchError = 'Please check the activation key and ' .
                           'try again.';
    }
    else {
        $keysMatch = 1;
        # TODO:  FIX, level no longer exists
        #$pendingLevel = $row->{level};
    }

    # If the input password and the activation key matched against those
    # in the database, activate the account.
    if ( $keysMatch ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $statement = "UPDATE users SET activationKey = '' WHERE login = ?";
        my $unused = $self->{db}->doQuery($statement, $login);
    }
    else {
        throw Error::Simple($nonMatchError);
    }
    $results->{msg} = 'The user account <strong>' .
       "$login</strong> has been successfully activated. You " .
       'will be redirected to the main service login page in 10 seconds. ' .
       '<br>Please change the password to your own once you sign in.';
    my $msg = $results->{msg};
    return( $msg, $results );
} #____________________________________________________________________________ 


######
1;
