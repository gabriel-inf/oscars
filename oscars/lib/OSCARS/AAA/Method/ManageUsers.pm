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

April 17, 2006

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
    $self->{param_tests} = {};
    $self->{param_tests}->{addUser} = {
        'selected_user' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
        'password_new_once' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's current password."
            }
        ),
        'user_last_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's last name."
            }
        ),
        'user_first_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's first name."
            }
        ),
        'institution_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's organization."
            }
        ),
        'user_email_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary email address."
            }
        ),
        'user_phone_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary phone number."
            }
        )
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  Gets all information necessary for the Manage Users page. 
#     It returns information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
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
        $results->{institution_list} =
                $self->{institutions}->get_institutions( $self->{db} );
    }
    elsif ($self->{params}->{op} eq 'viewUsers') {
        $results->{list} = $self->get_users($self->{params});
    }
    elsif ($self->{params}->{op} eq 'addUser') {
        $self->add_user( $self->{params} );
        $results->{list} = $self->get_users($self->{params});
        $msg = "$self->{user}->{login} added user $self->{params}->{selected_user}";
    }
    elsif ($self->{params}->{op} eq 'deleteUser') {
        $self->delete_user( $self->{params} );
        $results->{list} = $self->get_users($self->{params});
        $msg = "$self->{user}->{login} deleted user $self->{params}->{selected_user}";
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_users:  Retrieves all information from users table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub get_users {
    my( $self, $params ) = @_;

    my $statement = "SELECT * FROM users WHERE user_status != 'role' " .
                    "ORDER BY user_last_name";
    my $results = $self->{db}->do_query($statement);
    for my $oscars_user (@$results) {
        $oscars_user->{institution_name} = $self->{institutions}->get_name(
                        $self->{db}, $oscars_user->{institution_id});
	$oscars_user->{institution_id} = 'hidden';
        $oscars_user->{user_password} = 'hidden';
	$oscars_user->{user_id} = 'hidden';
    }
    return $results;
} #____________________________________________________________________________


###############################################################################
# add_user:  Add a user to the AAA database.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub add_user {
    my ( $self, $params ) = @_;

    my $results = {};
    $params->{user_login} = $params->{selected_user};

    # login name overlap check
    my $statement = 'SELECT user_login FROM users WHERE user_login = ?';
    my $row = $self->{db}->get_row($statement, $params->{user_login});
    if ( $row ) {
        throw Error::Simple("The login, $params->{user_login}, is already " .
	       	"taken by someone else; please choose a different login name.");
    }
    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $params->{institution_name} ) {
        $params->{institution_id} = $self->{institutions}->get_id($self->{db},
                                                  $params->{institution_name});
    }
    $params->{user_password} = crypt($params->{password_new_once}, 'oscars');
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->do_query( $statement );

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
    my $unused = $self->{db}->do_query($statement, @insertions);
    return;
} #____________________________________________________________________________


###############################################################################
# delete_user:  Deletes user with the given distinguished name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub delete_user {
    my( $self, $params ) = @_;

    # check to make sure user exists
    my $statement = 'SELECT user_login FROM users WHERE user_login = ?';
    my $row = $self->{db}->get_row($statement, $params->{selected_user});
    if ( !$row ) {
        throw Error::Simple("Cannot delete user $params->{selected_user}. " .
	       	"The user does not exist.");
    }
    my $statement = 'DELETE from users where user_login = ?';
    my $unused = $self->{db}->do_query($statement, $params->{selected_user});
    return;
} #____________________________________________________________________________


##########################################
# Following methods are not functional yet
##########################################


###############################################################################
# activate_account:  Activate a user's account.  Not functional.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub activate_account {
    my( $self, $params ) = @_;

    my ( $results) ;

    my $user_login = $self->{user}->{login};
    # get the password from the database
    my $statement = 'SELECT user_password, user_activation_key
                 FROM users WHERE user_login = ?';
    my $row = $self->{db}->get_row($statement, $user_login);

    # check whether this person is a registered user
    if ( !$row ) {
        throw Error::Simple('Please check your login name and try again.');
    }

    my $keys_match = 0;
    my( $pending_level, $non_match_error );
        # this login name is in the database; compare passwords
    if ( $row->{user_activation_key} eq '' ) {
        $non_match_error = 'This account has already been activated.';
    }
    elsif ( $row->{user_password} ne $params->{user_password} ) {
        $non_match_error = 'Please check your password and try again.';
    }
    elsif ( $row->{user_activation_key} ne 
            $params->{user_activation_key} ) {
        $non_match_error = 'Please check the activation key and ' .
                           'try again.';
    }
    else {
        $keys_match = 1;
        # TODO:  FIX, user_level no longer exists
        #$pending_level = $row->{user_level};
    }

    # If the input password and the activation key matched against those
    # in the database, activate the account.
    if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $statement = "UPDATE users
                  user_activation_key = '' WHERE user_login = ?";
        my $unused = $self->{db}->do_query($statement, $user_login);
    }
    else {
        throw Error::Simple($non_match_error);
    }
    $results->{status_msg} = 'The user account <strong>' .
       "$user_login</strong> has been successfully activated. You " .
       'will be redirected to the main service login page in 10 seconds. ' .
       '<br>Please change the password to your own once you sign in.';
    my $msg = $results->{status_msg};
    return( $msg, $results );
} #____________________________________________________________________________ 


###############################################################################
# process_registration:  Process a user's registration.  Not functional
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub process_registration {
    my( $self, $params, @insertions ) = @_;

    my $results = {};
    my $user_login = $self->{user}->{login};

    my $encrypted_password = $params->{password_new_once};

    # get current date/time string in GMT
    my $current_date_time = $params->{utc_seconds};
    # login name overlap check
    my $statement = 'SELECT user_login FROM users WHERE user_login = ?';
    my $row = $self->{db}->get_row($statement, $user_login);
    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }
    $statement = 'INSERT INTO users VALUES ( ' .
                              '?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )';
    my $unused = $self->{db}->do_query($statement, @insertions);

    $results->{status_msg} = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$user_login</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    my $msg = $results->{status_msg};
    return( $msg, $results );
} #____________________________________________________________________________


######
1;
