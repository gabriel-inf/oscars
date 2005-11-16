package AAAS::Frontend::SOAPMethods;

# SOAPMethods.pm: SOAP methods callable from dispatcher.
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use SOAP::Lite;

use AAAS::Frontend::Database;
use AAAS::Frontend::Auth;

# until can get MySQL 5 and views going

# names of the fields to be displayed on the screen
my @user_profile_fields = ( 'user_last_name',
                            'user_first_name',
                            'user_dn',
                            'user_password',
                            'user_email_primary',
#                            'user_level',
                            'user_email_secondary',
                            'user_phone_primary',
                            'user_phone_secondary',
                            'user_description',
#                            'user_register_time',
#                            'user_activation_key',
                            'institution_id');

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;
    $self->{auth} = AAAS::Frontend::Auth->new(
                       'dbconn' => $self->{dbconn});
    $self->{target} = SOAP::Lite
                       -> uri('http://198.128.14.164/Dispatcher')
                       -> proxy ('https://198.128.14.164/BSS');
}
######


####################################
# Methods that need user privileges.
####################################

###############################################################################
# verify_login
# In:  reference to hash of parameters
# Out: status code, status message
#
sub verify_login {
    my( $self, $inref ) = @_;

    my $results = {};
    my $user_dn = $inref->{user_dn};

    if (!$self->{auth}->authorized($user_dn, 'verify_login')) {
        throw Error::Simple("User not authorized to verify login");
    }

    # Get the password and privilege level from the database.
    my $statement = "SELECT user_password, user_level FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($statement, $user_dn);
    # Make sure user exists.
    if (!@$rows) {
        throw Error::Simple("Please check your login name and try again.");
    }
    # compare passwords
    my $encoded_password = crypt($inref->{user_password}, 'oscars');
    if ( $rows->[0]->{user_password} ne $encoded_password ) {
        throw Error::Simple("Please check your password and try again.");
    }
    return( $results );
}
######

##############################################################################
# get_profile
# In:  reference to hash of parameters
# Out: status code, status message
#
sub get_profile {
    my( $self, $inref ) = @_;

    my( $results );

    if (!$self->{auth}->authorized($inref->{user_dn}, 'get_profile')) {
        throw Error::Simple("User not authorized for get_profile");
    }
    # DB query: get the user profile detail
    my $statement = "SELECT " . join(', ', @user_profile_fields) .
             " FROM users where user_dn = ?";

    my $rows = $self->{dbconn}->do_query($statement, $inref->{user_dn});

    # check whether this person is a registered user
    # (love that syntax:  testing for rows will not work because ref not
    #  empty)
    if (!@$rows) {
        throw Error::Simple("No such user.");
    }

    $statement = "SELECT institution_name FROM institutions
              WHERE institution_id = ?";
    my $irows = $self->{dbconn}->do_query($statement,
                                     $rows->[0]->{institution_id});

    # check whether this organization is in the db
    if (!@$irows) {
        throw Error::Simple("No such organization recorded.");
    }
    $results = @{$rows}[0];
    $results->{institution} = $irows->[0]->{institution};
    # X out password
    $results->{user_password} = undef;
    return ( $results );
}
######

###############################################################################
# set_profile
# In:  reference to hash of parameters
# Out: status code, status message
#
sub set_profile {
    my ( $self, $inref ) = @_;

    my $user_dn = $inref->{user_dn};

        # TODO:  setting other person's profile (admin)
    if (!$self->{auth}->authorized($user_dn, 'set_profile')) {
        throw Error::Simple("User not authorized for set_profile");
    }

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    my $statement = "SELECT " . join(', ', @user_profile_fields) .
                ", user_level FROM users where user_dn = ?";
    my $results = $self->{dbconn}->do_query($statement, $user_dn);

    # check whether this person is in the database
    if (!@$results) {
        throw Error::Simple("The user $user_dn does not have an OSCARS login.");
    }

    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $results->[0]->{user_password} ne
         crypt($inref->{user_password},'oscars') ) {
        throw Error::Simple("Please check the current password and " .
                                "try again.");
    }

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $inref->{password_new_once} ) {
        $inref->{user_password} = crypt( $inref->{password_new_once},
                                         'oscars');
    }
    else {
        $inref->{user_password} = crypt($inref->{user_password}, 'oscars');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions.
    if ( $inref->{institution} ) {
        $self->{dbconn}->get_institution_id($inref, $inref->{user_dn});
    }

    # prepare the query for database update
    $statement = "UPDATE users SET ";
    for $_ (@user_profile_fields) {
        $statement .= "$_ = '$inref->{$_}', ";
        # TODO:  check that query preparation correct
        $results->[0]->{$_} = $inref->{$_};
    }
    $statement =~ s/,\s$//;
    $statement .= " WHERE user_dn = ?";
    my $unused = $self->{dbconn}->do_query($statement, $user_dn);

    $results->[0]->{institution} = $inref->{institution};
    $results->[0]->{user_password} = undef;
    return( $results );
}
######

##############################################
# Methods requiring administrative privileges.
##############################################

###############################################################################
# add_user:  when doing directly from "add user" page, rather than going 
#            through the registration process
#
# In:  reference to hash of parameters
# Out: status code, status message
#
sub add_user {
    my ( $self, $inref ) = @_;

    my $results = {};
    my $user_dn = $inref->{user_dn};

    if (!$self->{auth}->authorized($inref->{user_dn}, 'add_user')) {
        throw Error::Simple("User not authorized to add another user");
    }
    print STDERR "add_user\n";
    my $encrypted_password = $inref->{password_once};

    # login name overlap check
    my $statement = "SELECT user_dn FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($statement, $user_dn);

    if ( scalar(@$rows) > 0 ) {
        throw Error::Simple("The login, $user_dn, is already taken " .
                   "by someone else; please choose a different login name.");
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $inref->{institution} ) {
        $self->{dbconn}->get_institution_id($inref, $inref->{user_dn});
    }

    $inref->{user_password} = crypt($inref->{password_new_once}, 'oscars');
    $statement = "SHOW COLUMNS from users";
    $rows = $self->{dbconn}->do_query( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($inref->{$_->{Field}}) {
           $results->{$_->{Field}} = $inref->{$_->{Field}};
           push(@insertions, $inref->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    $statement = "INSERT INTO users VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
             
    my $unused = $self->{dbconn}->do_query($statement, @insertions);
    # X out password
    $results->{user_password} = undef;
    return( $results );
}
######

###############################################################################
# get_userlist
# In:  inref
# Out: status message and DB results
#
sub get_userlist {
    my( $self, $inref ) = @_;

    my( $r, $irows, $user );
    my $user_dn = $inref->{user_dn};

    if (!$self->{auth}->authorized($user_dn, 'get_userlist')) {
        throw Error::Simple("User not authorized to get list of users");
    }
    my $statement .= "SELECT * FROM users ORDER BY user_last_name";
    my $user_rows = $self->{dbconn}->do_query($statement);

    for $user (@$user_rows) {
        # replace institution id with institution name
        $statement = "SELECT institution_name FROM institutions " .
                 "WHERE institution_id = ?";
        $irows = $self->{dbconn}->do_query($statement, $user->{institution_id});
        $user->{institution_id} = $irows->[0]->{institution_name};
        $user->{user_password} = undef;
    }
    return( $user_rows );
}
######

################################################
# Registration methods (require admin permission).
################################################

###############################################################################
# activate_account
# In:  reference to hash of parameters
# Out: status code, status message
#
sub activate_account {
    my( $self, $inref, $required_level ) = @_;

    my ( $r, $results) ;
    my $user_dn = $inref->{user_dn};

    $self->{auth}->authorized($inref->{user_level}, $required_level, 1);

    # get the password from the database
    my $statement = "SELECT user_password, user_activation_key, user_level
                 FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($statement, $user_dn);

    # check whether this person is a registered user
    if (!$rows) {
        throw Error::Simple("Please check your login name and try again.");
    }

    my $keys_match = 0;
    my( $pending_level, $non_match_error );
        # this login name is in the database; compare passwords
    for $r (@$rows) {
        if ( $r->[0]->{user_activation_key} eq '' ) {
            $non_match_error = 'This account has already been activated.';
        }
        elsif ( $r->[0]->{user_password} ne $inref->{user_password} ) {
            $non_match_error = 'Please check your password and try again.';
        }
        elsif ( $r->[0]->{user_activation_key} ne $inref->{user_activation_key} ) {
            $non_match_error = "Please check the activation key and " .
                               "try again.";
        }
        else {
            $keys_match = 1;
            $pending_level = $r->[0]->{user_level};
        }
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
    $results->{status_msg} = "The user account <strong>" .
       "$user_dn</strong> has been successfully activated. You " .
       "will be redirected to the main service login page in 10 seconds. " .
       "<br>Please change the password to your own once you sign in.";
    return( $results );
}
######

###############################################################################
# process_registration
# In:  reference to hash of parameters
# Out: status message
#
sub process_registration {
    my( $self, $inref, @insertions ) = @_;

    my $results = {};
    my $user_dn = $inref->{user_dn};

    my $encrypted_password = $inref->{password_once};

    # get current date/time string in GMT
    my $current_date_time = $inref->{utc_seconds};
	
    # login name overlap check
    my $statement = "SELECT user_dn FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($statement, $user_dn);

    if ( scalar(@$rows) > 0 ) {
        throw Error::Simple("The selected login name is already taken " .
                   "by someone else; please choose a different login name.");
    }

    $statement = "INSERT INTO users VALUES ( " .
                              "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    my $unused = $self->{dbconn}->do_query($statement, @insertions);

    $results->{status_msg} = "Your user registration has been recorded " .
        "successfully. Your login name is <strong>$user_dn</strong>. Once " .
        "your registration is accepted, information on " .
        "activating your account will be sent to your primary email address.";
    return( $results );
}
######

##############################################
# Calls to the BSS.
##############################################

sub insert_reservation {
    my( $self, $form_params ) = @_;

    if (!$self->{auth}->authorized($form_params->{user_dn},
                                    'insert_reservation')) {
        throw Error::Simple("User not authorized to schedule reservation");
    }
    my $som = $self->{target}->dispatch($form_params);
    return( $som->result );
}

sub delete_reservation {
    my( $self, $form_params ) = @_;

    if (!$self->{auth}->authorized($form_params->{user_dn},
                                    'delete_reservation')) {
        throw Error::Simple("User not authorized to delete reservation");
    }
    my $som = $self->{target}->dispatch($form_params);
    return( $som->result );
}

sub get_reservations {
    my( $self, $form_params ) = @_;

    if (!$self->{auth}->authorized($form_params->{user_dn},
                                    'insert_reservation')) {
        throw Error::Simple("User not authorized to get list of reservations");
    }
    my $som = $self->{target}->dispatch($form_params);
    return( $som->result );
}

######
1;
