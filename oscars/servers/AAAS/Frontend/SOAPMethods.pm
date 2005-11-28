###############################################################################
package AAAS::Frontend::SOAPMethods;

# AAAS SOAP methods callable from AAAS::SOAP::Dispatcher.  Authorization and 
# parameter validation are performed by the dispatcher.
#
# Last modified:  November 22, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use SOAP::Lite;

use AAAS::Frontend::Database;

# until can get MySQL 5 and views going

# names of the fields to be displayed on the screen
my $user_profile_fields =
     'user_last_name, user_first_name, user_dn, user_password, ' .
     'user_email_primary, user_email_secondary, ' .
     'user_phone_primary, user_phone_secondary, user_description, ' .
#    'user_register_time, user_activation_key, ' .
     'institution_id';


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


###############################################################################
# login:  Log in to OSCARS.
#
# In:  reference to hash of parameters
# Out: reference to hash of results containing user dn and user level.
#
sub login {
    my( $self, $params ) = @_;

    my $user_dn = $params->{user_dn};

    # Get the password and privilege level from the database.
    my $statement = 'SELECT user_password, user_level FROM users WHERE user_dn = ?';
    my $results = $self->{dbconn}->get_row($statement, $user_dn);
    # Make sure user exists.
    if ( !$results ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    # compare passwords
    my $encoded_password = crypt($params->{user_password}, 'oscars');
    if ( $results->{user_password} ne $encoded_password ) {
        throw Error::Simple('Please check your password and try again.');
    }
    $results->{user_dn} = $user_dn;
    # X out password
    $results->{user_password} = undef;
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# get_profile:  Gets the user profile from the database.  If the user has
#     admin privileges, show all fields.  If the user is an admin, they can
#     request the profile of another user.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub get_profile {
    my( $self, $params ) = @_;

    my( $statement, $results );

    if ( $params->{admin_permission} ) {
        $statement = 'SELECT * FROM users where user_dn = ?';
        # coming in from view users list
        if( $params->{id} ) {
            $results = $self->{dbconn}->get_row($statement, $params->{id});
        }
        else {
            $results = $self->{dbconn}->get_row($statement, $params->{user_dn});
        }
    }
    else {
        $statement = "SELECT $user_profile_fields FROM users where user_dn = ?";
        $results = $self->{dbconn}->get_row($statement, $params->{user_dn});
    }

    # check whether this person is a registered user
    # (love that syntax:  testing for rows will not work because ref not
    #  empty)
    if ( !$results ) {
        throw Error::Simple("No such user $params->{user_dn}.");
    }
    if ( $params->{id} ) {
        $results->{id} = $params->{id};
    }

    $statement = 'SELECT institution_name FROM institutions
              WHERE institution_id = ?';
    my $irow = $self->{dbconn}->get_row($statement,
                                        $results->{institution_id});

    # check whether this organization is in the db
    if ( !$irow ) {
        throw Error::Simple( 'Organization not found.' );
    }
    $results->{institution_id} = $irow->{institution_name};
    # X out password
    $results->{user_password} = undef;
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# set_profile:  Modifies the user profile for a particular user.  If the
#     user has admin privileges, they can set the information for another
#     user.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub set_profile {
    my ( $self, $params ) = @_;

    my $user_dn = $params->{user_dn};

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    my $statement = "SELECT $user_profile_fields FROM users where user_dn = ?";
    my $results = $self->{dbconn}->get_row($statement, $user_dn);

    # check whether this person is in the database
    if ( !$results ) {
        throw Error::Simple("User $user_dn does not have an OSCARS login.");
    }

    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $results->{user_password} ne
         crypt($params->{user_password}, 'oscars') ) {
        throw Error::Simple(
            'Please check the current password and try again.');
    }

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $params->{password_new_once} ) {
        $params->{user_password} = crypt( $params->{password_new_once},
                                         'oscars');
    }
    else {
        $params->{user_password} = crypt($params->{user_password}, 'oscars');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions.
    if ( $params->{institution} ) {
        $self->{dbconn}->get_institution_id($params, $params->{user_dn});
    }

    # prepare the query for database update
    $statement = 'UPDATE users SET ';
    my @fields = split(', ', $user_profile_fields);
    for $_ (@fields) {
        $statement .= "$_ = '$params->{$_}', ";
        # TODO:  check that query preparation correct
        $results->{$_} = $params->{$_};
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE user_dn = ?';
    my $unused = $self->{dbconn}->do_query($statement, $user_dn);

    $results->{institution} = $params->{institution};
    $results->{user_password} = undef;
    return $results;
} #____________________________________________________________________________ 


#################################################################
# The following methods can only be accessed by a user with admin
# privileges.
#################################################################


###############################################################################
# add_user:  Add a user to the OSCARS database.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub add_user {
    my ( $self, $params ) = @_;

    my $results = {};
    my $user_dn = $params->{user_dn};

    my $encrypted_password = $params->{password_once};

    # login name overlap check
    my $statement = 'SELECT user_dn FROM users WHERE user_dn = ?';
    my $row = $self->{dbconn}->get_row($statement, $user_dn);

    if ( $row ) {
        throw Error::Simple("The login, $user_dn, is already taken " .
                   'by someone else; please choose a different login name.');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $params->{institution} ) {
        $self->{dbconn}->get_institution_id($params, $params->{user_dn});
    }

    $params->{user_password} = crypt($params->{password_new_once}, 'oscars');
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{dbconn}->do_query( $statement );

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
             
    my $unused = $self->{dbconn}->do_query($statement, @insertions);
    # X out password
    $results->{user_password} = undef;
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# view_users:  Retrieves the profile information for all users.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub view_users {
    my( $self, $params ) = @_;

    my( $r, $irow, $user );
    my $user_dn = $params->{user_dn};

    my $statement .= 'SELECT * FROM users ORDER BY user_last_name';
    my $results = $self->{dbconn}->do_query($statement);

    for $user (@$results) {
        # replace institution id with institution name
        $statement = 'SELECT institution_name FROM institutions ' .
                 'WHERE institution_id = ?';
        $irow = $self->{dbconn}->get_row($statement, $user->{institution_id});
        $user->{institution_id} = $irow->{institution_name};
        $user->{user_password} = undef;
    }
    return $results;
} #____________________________________________________________________________ 


####################################################################
# Registration methods (not functional; require Shibboleth, and code
# modifications).
####################################################################

###############################################################################
# activate_account:  Activate a user's account.  Not functional.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub activate_account {
    my( $self, $params, $required_level ) = @_;

    my ( $results) ;
    my $user_dn = $params->{user_dn};

    # get the password from the database
    my $statement = 'SELECT user_password, user_activation_key, user_level
                 FROM users WHERE user_dn = ?';
    my $row = $self->{dbconn}->get_row($statement, $user_dn);

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
    elsif ( $row->{user_activation_key} ne $params->{user_activation_key} ) {
        $non_match_error = 'Please check the activation key and ' .
                           'try again.';
    }
    else {
        $keys_match = 1;
        $pending_level = $row->{user_level};
    }

    # If the input password and the activation key matched against those
    # in the database, activate the account.
    if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $statement = "UPDATE users SET user_level = ?, pending_level = ?,
                  user_activation_key = '' WHERE user_dn = ?";
        my $unused = $self->{dbconn}->do_query($statement, $pending_level,
                                         $user_dn);
    }
    else {
        throw Error::Simple($non_match_error);
    }
    $results->{status_msg} = 'The user account <strong>' .
       "$user_dn</strong> has been successfully activated. You " .
       'will be redirected to the main service login page in 10 seconds. ' .
       '<br>Please change the password to your own once you sign in.';
    return $results;
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
    my $user_dn = $params->{user_dn};

    my $encrypted_password = $params->{password_once};

    # get current date/time string in GMT
    my $current_date_time = $params->{utc_seconds};
	
    # login name overlap check
    my $statement = 'SELECT user_dn FROM users WHERE user_dn = ?';
    my $row = $self->{dbconn}->get_row($statement, $user_dn);

    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }

    $statement = 'INSERT INTO users VALUES ( ' .
                              '?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )';
    my $unused = $self->{dbconn}->do_query($statement, @insertions);

    $results->{status_msg} = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$user_dn</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    return $results;
} #____________________________________________________________________________ 


######
1;
