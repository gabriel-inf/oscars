package AAAS::Frontend::SOAPMethods;

# SOAPMethods.pm: SOAP methods callable from dispatcher.
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use AAAS::Frontend::Database;
use AAAS::Frontend::Registration;
use AAAS::Frontend::Auth;
use AAAS::Frontend::Forwarder;

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
    $self->{registr} = AAAS::Frontend::Registration->new(
                       'dbconn' => $self->{dbconn},
                       'auth' => $self->{auth});
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
        throw Common::Exception("User not authorized to verify login");
    }

    # Get the password and privilege level from the database.
    my $query = "SELECT user_password, user_level FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($query, $user_dn);
    # Make sure user exists.
    if (!$rows) {
        throw Common::Exception("Please check your login name and try again.");
    }
    # compare passwords
    my $encoded_password = crypt($inref->{user_password}, 'oscars');
    if ( $rows->[0]->{user_password} ne $encoded_password ) {
        throw Common::Exception("Please check your password and try again.");
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
        throw Common::Exception("User not authorized for get_profile");
    }
    # DB query: get the user profile detail
    my $query = "SELECT " . join(', ', @user_profile_fields) .
             " FROM users where user_dn = ?";

    my $rows = $self->{dbconn}->do_query($query, $inref->{user_dn});

    # check whether this person is a registered user
    if (!$rows) {
        throw Common::Exception("No such user.");
    }

    $query = "SELECT institution_name FROM institutions
              WHERE institution_id = ?";
    my $irows = $self->{dbconn}->do_query($query,
                                     $rows->[0]->{institution_id});

    # check whether this organization is in the db
    if (!$irows) {
        throw Common::Exception("No such organization recorded.");
    }

    # TODO:  FIX weird results assignments
    $results->{row} = @{$rows}[0];
    $results->{row}->{institution} = $irows->[0]->{institution};
    # X out password
    $results->{row}->{user_password} = undef;
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

    my $results;
    my $user_dn = $inref->{user_dn};

        # TODO:  setting other person's profile (admin)
    if (!$self->{auth}->authorized($user_dn, 'set_profile')) {
        throw Common::Exception("User not authorized for set_profile");
    }

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    my $query = "SELECT " . join(', ', @user_profile_fields) .
                ", user_level FROM users where user_dn = ?";
    my $rows = $self->{dbconn}->do_query($query, $user_dn);

    # check whether this person is in the database
    if (!$rows) {
        throw Common::Exception("The user $user_dn does not have an OSCARS login.");
    }

    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $rows->[0]->{user_password} ne
         crypt($inref->{user_password},'oscars') ) {
        throw Common::Exception("Please check the current password and " .
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
    $query = "UPDATE users SET ";
    for $_ (@user_profile_fields) {
        $query .= "$_ = '$inref->{$_}', ";
        # TODO:  check that query preparation correct
        $rows->[0]->{$_} = $inref->{$_};
    }
    $query =~ s/,\s$//;
    $query .= " WHERE user_dn = ?";
    my $unused = $self->{dbconn}->do_query($query, $user_dn);

    $rows->[0]->{institution} = $inref->{institution};
    $rows->[0]->{user_password} = undef;
    $results->{row} = $rows;
    return( $results );
}
######

##############################################
# Methods requiring administrative privileges.
##############################################

###############################################################################
# add_user
# In:  reference to hash of parameters
# Out: status code, status message
#
sub add_user {
    my ( $self, $inref ) = @_;

    if (!$self->{auth}->authorized($inref->{user_dn}, 'add_user')) {
        throw Common::Exception("User not authorized to add another user");
    }
    return $self->{registr}->add_user($inref);
}
######

###############################################################################
# get_userlist
# In:  inref
# Out: status message and DB results
#
sub get_userlist {
    my( $self, $inref ) = @_;

    my( $r, $results, $irows, $user );
    my $user_dn = $inref->{user_dn};

    if (!$self->{auth}->authorized($user_dn, 'get_userlist')) {
        throw Common::Exception("User not authorized to get list of users");
    }
    my $query .= "SELECT * FROM users ORDER BY user_last_name";
    my $user_rows = $self->{dbconn}->do_query($query);

    for $user (@$user_rows) {
        # replace institution id with institution name
        $query = "SELECT institution_name FROM institutions " .
                 "WHERE institution_id = ?";
        $irows = $self->{dbconn}->do_query($query, $user->{institution_id});
        $user->{institution_id} = $irows->[0]->{institution_name};
        $user->{user_password} = undef;
    }

    $results->{rows} = $user_rows;
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
        throw Common::Exception("User not authorized to schedule reservation");
    }
    my $som = forward($form_params);
    return( $som->result );
}

sub delete_reservation {
    my( $self, $form_params ) = @_;

    if (!$self->{auth}->authorized($form_params->{user_dn},
                                    'delete_reservation')) {
        throw Common::Exception("User not authorized to delete reservation");
    }
    my $som = forward($form_params);
    return( $som->result );
}

sub get_reservations {
    my( $self, $form_params ) = @_;

    if (!$self->{auth}->authorized($form_params->{user_dn},
                                    'insert_reservation')) {
        throw Common::Exception("User not authorized to get list of reservations");
    }
    my $som = forward($form_params);
    return( $som->result );
}

######
1;
