#==============================================================================
package OSCARS::AAA::Method::ManageUsers;

=head1 NAME

OSCARS::AAA::Method::ManageUsers - Handles reservation system users.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::ManageUsers;

=head1 DESCRIPTION

SOAP method to view information about all users.  It inherits from 
OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 18, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::AAA::Method::ManageInstitutions;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::ManageInstitutions->new();
    $self->{paramTests} = {};
    $self->{paramTests}->{addUser} = {
        'selectedUser' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
        'passwordNewOnce' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's current password."
            }
        ),
        'lastName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's last name."
            }
        ),
        'firstName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's first name."
            }
        ),
        'institutionName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's organization."
            }
        ),
        'emailPrimary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary email address."
            }
        ),
        'phonePrimary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary phone number."
            }
        )
    };
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
    if ( !$self->{params}->{op} ) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires specification of an operation");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'addUserForm') {
        $results->{institutionList} =
                $self->{institutions}->queryInstitutions( $self->{db} );
    }
    elsif ($self->{params}->{op} eq 'listUsers') {
        $results->{list} = $self->getUsers($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addUser') {
        $self->addUser( $self->{params} );
        $results->{list} = $self->getUsers($self->{params});
        $msg = "$self->{user}->{login} added user $self->{params}->{selectedUser}";
    }
    elsif ($self->{params}->{op} eq 'deleteUser') {
        $self->deleteUser( $self->{params} );
        $results->{list} = $self->getUsers($self->{params});
        $msg = "$self->{user}->{login} deleted user $self->{params}->{selectedUser}";
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# getUsers:  Retrieves all information from users table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub getUsers {
    my( $self, $params ) = @_;

    my $statement = "SELECT * FROM users WHERE status != 'role' " .
                    "ORDER BY lastName";
    my $results = $self->{db}->doQuery($statement);
    for my $oscarsUser (@$results) {
        $oscarsUser->{institutionName} = $self->{institutions}->getName(
                        $self->{db}, $oscarsUser->{institutionId});
	$oscarsUser->{institutionId} = 'hidden';
        $oscarsUser->{password} = 'hidden';
	$oscarsUser->{id} = 'hidden';
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# addUser:  Add a user to the AAA database.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub addUser {
    my ( $self, $params ) = @_;

    my $results = {};
    $params->{login} = $params->{selectedUser};

    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $params->{login});
    if ( $row ) {
        throw Error::Simple("The login, $params->{login}, is already " .
	       	"taken by someone else; please choose a different login name.");
    }
    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $params->{institutionName} ) {
        $params->{institutionId} = $self->{institutions}->getId($self->{db},
                                                  $params->{institutionName});
    }
    $params->{password} = crypt($params->{passwordNewOnce}, 'oscars');
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->doQuery( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }
    $statement = "INSERT INTO users VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{db}->doQuery($statement, @insertions);
    return;
} #____________________________________________________________________________


###############################################################################
# deleteUser:  Deletes user with the given distinguished name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub deleteUser {
    my( $self, $params ) = @_;

    # check to make sure user exists
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $params->{selectedUser});
    if ( !$row ) {
        throw Error::Simple("Cannot delete user $params->{selectedUser}. " .
	       	"The user does not exist.");
    }
    my $statement = 'DELETE from users where login = ?';
    my $unused = $self->{db}->doQuery($statement, $params->{selectedUser});
    return;
} #____________________________________________________________________________


##########################################
# Following methods are not functional yet
##########################################


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


###############################################################################
# processRegistration:  Process a user's registration.  Not functional
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub processRegistration {
    my( $self, $params, @insertions ) = @_;

    my $results = {};
    my $login = $self->{user}->{login};

    my $encryptedPassword = $params->{passwordNewOnce};

    # get current date/time string in GMT
    my $currentDateTime = $params->{utcSeconds};
    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);
    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }
    $statement = 'INSERT INTO users VALUES ( ' .
                              '?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )';
    my $unused = $self->{db}->doQuery($statement, @insertions);

    $results->{msg} = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$login</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    my $msg = $results->{msg};
    return( $msg, $results );
} #____________________________________________________________________________


######
1;
