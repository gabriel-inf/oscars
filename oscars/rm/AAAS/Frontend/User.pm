package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with admin and user forms.
# Last modified: June 8, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;

use AAAS::Frontend::Database;
use Data::Dumper;

# until can get MySQL 5 and views going

# names of the fields to be displayed on the screen
my @user_profile_fields = ( 'user_last_name',
                            'user_first_name',
                            'user_dn',
                            'user_password',
                            'user_email_primary',
                            'user_level',
                            'user_email_secondary',
                            'user_phone_primary',
                            'user_phone_secondary',
                            'user_description',
                            'user_register_time',
                            'user_activation_key',
                            'institution_id');


###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;
    $self->{dbconn} = AAAS::Frontend::Database->new(
                           'configs' => $self->{configs})
                        or die "FATAL:  could not connect to database";
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

    my( $query, $sth, $ref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 1, 1);
    if ($results->{error_msg}) { return( 1, $results); }
    # Get user levels
    if (!defined($self->{user_levels})) {
        ($self->{user_levels}, $results->{error_msg}) = $self->{dbconn}->get_user_levels($user_dn);
        if ($results->{error_msg}) { return( 1, $results); }
    }

    # Get the password and privilege level from the database.
    $query = "SELECT user_password, user_level FROM users WHERE user_dn = ?";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }
    # Make sure user exists.
    if (!$sth->rows) {
        $sth->finish();
        $results->{error_msg} = 'Please check your login name and try again.';
        return (1, $results);
    }
    # compare passwords
    my $encoded_password = crypt($inref->{user_password}, 'oscars');
    $ref = $sth->fetchrow_hashref();
    if ( $ref->{user_password} ne $encoded_password ) {
        $results->{error_msg} = 'Please check your password and try again.';
    }
    $sth->finish();
    $results->{error_msg} = $self->check_authorization($ref->{user_level},
                                                 $self->{user_levels}->{user});
    if ($results->{error_msg}) { return( 1, $results ); }

    $results->{user_level} = $self->get_str_level($ref->{user_level});
    $results->{status_msg} = 'The user has successfully logged in.';
    # The first value is unused, but I can't get SOAP to send a correct
    # reply without it so far.
    return( 0, $results );
}
######

###############################################################################
sub logout {
    my( $self, $params ) = @_;

    return( $self->{dbconn}->logout($params->{user_dn}) );
}
######

##############################################################################
# get_profile
# In:  reference to hash of parameters
# Out: status code, status message
#
sub get_profile {
    my( $self, $inref ) = @_;

    my( $sth, $query, $rref );
    my( @data );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->check_authorization(
                                $self->get_numeric_level($inref->{user_level}),
                                $self->{user_levels}->{user});
    if ($results->{error_msg}) { return( 1, $results ); }

    # DB query: get the user profile detail
    $query = "SELECT " . join(', ', @user_profile_fields);
    $query .= " FROM users where user_dn = ?";

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results->{error_msg} = 'No such user.';
        return (1, $results);
    }

    # populate results with the data fetched from the database
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $query = "SELECT institution_name FROM institutions
              WHERE institution_id = ?";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                     @{$rref}[0]->{institution_id});
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # check whether this organization is in the db
    if (!$sth->rows) {
        $sth->finish();
        $results->{error_msg} = 'No such organization recorded.';
        return (1, $results);
    }

    @data = $sth->fetchrow_array();
    $results->{institution} = $data[0];
    $sth->finish();

    $results->{user_level} = $self->get_str_level($results->{user_level}) ;
    $results->{rows} = $rref;
    $results->{status_msg} = 'Retrieved user profile';
    return ( 0, $results );
}
######

###############################################################################
# set_profile
# In:  reference to hash of parameters
# Out: status code, status message
#
sub set_profile {
    my ( $self, $inref, $fields_to_read ) = @_;

    my( $sth, $query, $read_only_level, @read_only_user_levels );
    my( $current_info, $ref, $do_update );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    # TODO:  make sure user_level not in fields_to_read already
    $query = "SELECT user_level, " . join(', ', @$fields_to_read);
    $query .= " FROM users WHERE user_dn = ?";

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # check whether this person is a registered user
    if (!$sth->rows) {
        $results->{error_msg} = "The user $user_dn is not " .
                                "registered.";
        $sth->finish();
        return (1, $results);
    }

    $current_info = $sth->fetchrow_hashref;
    # make sure user has appropriate privileges
    # TODO:  if admin_dn set, need admin privileges
    $results->{error_msg} = $self->check_authorization(
                                $current_info->{user_level},
                                $self->{user_levels}->{user});
    if ($results->{error_msg}) { return( 1, $results ); }


    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $current_info->{user_password} ne
             crypt($inref->{user_password},'oscars') ) {
        $results->{error_msg} = "Please check the current password and " .
                                "try again.";
        $sth->finish();
        return( 1, $results);
    }
    $sth->finish();

    $do_update = 0;

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $inref->{password_new_once} ) {
        $inref->{user_password} = crypt( $inref->{password_new_once},
                                         'oscars');
        $do_update = 1;
    }
    else {
        $inref->{user_password} = crypt($inref->{user_password}, 'oscars');
    }

    # Check to see if the institution name provided is in the database.
    # If so, set the institution id to the primary key in the institutions
    # table.
    if ( $inref->{institution} ) {
        $query = "SELECT institution_id FROM institutions
                  WHERE institution_name = ?";
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                        $query,
                                                        $inref->{institution});
        if ( $results->{error_msg} ) { return( 1, $results ); }
        if (!$sth->rows) {
            $results->{error_msg} = "The organization " .
                            "$inref->{institution} is not in the database.";
            $sth->finish();
            return (1, $results);
        }
        $ref = $sth->fetchrow_hashref;
        $inref->{institution_id} = $ref->{institution_id} ;
        $results->{institution} = $inref->{institution}
    }

    # Compare the current & newly input user profile data and determine
    # which fields/values to update.  Assign results at this time also.
    for $_ ( @$fields_to_read ) {
        if ( $current_info->{$_} ne $inref->{$_} ) {
            $do_update = 1;
        }
        $results->{$_} = $inref->{$_};
    }
    $results->{user_password} = '';   # unset before passing back

    # if there is nothing to update...
    if ( !$do_update ) {
        $results->{error_msg} = 'There is no changed information to update.';
        return( 1, $results );
    }

    # prepare the query for database update
    $query = "UPDATE users SET ";
    for $_ ( @$fields_to_read ) {
        if (defined($inref->{$_} )) {
            $query .= "$_ = '$inref->{$_}', ";
        }
    }
    $query =~ s/,\s$//;
    $query .= " WHERE user_dn = ?";

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    $sth->finish();
    $results->{status_msg} = "The account information for user " .
                          "$user_dn has been updated successfully.";
    return( 0, $results );
}
######

###############################################################################
# activate_account
# In:  reference to hash of parameters
# Out: status code, status message
#
sub activate_account {
    my( $self, $inref ) = @_;

    my( $sth, $query );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->check_authorization(
                                $self->get_numeric_level($inref->{user_level}),
                                $self->{user_levels}->{user});
    if ($results->{error_msg}) { return( 1, $results ); }

    # get the password from the database
    $query = "SELECT user_password, user_activation_key, user_level
              FROM users WHERE user_dn = ?";

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results->{error_msg} = 'Please check your login name and try again.';
        return (1, $results);
    }

    my $keys_match = 0;
    my( $pending_level, $non_match_error );
        # this login name is in the database; compare passwords
    while ( my $ref = $sth->fetchrow_arrayref ) {
        if ( $$ref[1] eq '' ) {
            $non_match_error = 'This account has already been activated.';
        }
        elsif ( $$ref[0] ne $inref->{user_password} ) {
            $non_match_error = 'Please check your password and try again.';
        }
        elsif ( $$ref[1] ne $inref->{user_activation_key} ) {
            $non_match_error = "Please check the activation key and " .
                               "try again.";
        }
        else {
            $keys_match = 1;
            $pending_level = $$ref[2];
        }
    }
    $sth->finish();

    # If the input password and the activation key matched against those
    # in the database, activate the account.
    if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $query = "UPDATE users SET user_level = ?, pending_level = ?,
                  user_activation_key = '' WHERE user_dn = ?";
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                            $query, $pending_level, $user_dn);
        if ( $results->{error_msg} ) { return( 1, $results ); }
    }
    else {
        $sth->finish();
        $results->{error_msg} = $non_match_error;
        return( 1, $results );
    }
    $sth->finish();
    $results->{status_msg} = "The user account <strong>" .
       "$user_dn</strong> has been successfully activated. You " .
       "will be redirected to the main service login page in 10 seconds. " .
       "<br>Please change the password to your own once you sign in.";
    return( 0, $results );
}
######

###############################################################################
#
sub get_str_level {
    my( $self, $level_flag ) = @_;

    my $level = "";
    $level_flag += 0;
    if ($self->{user_levels}->{admin} & $level_flag) {
        $level .= 'admin ';
    }
    if ($self->{user_levels}->{engr} & $level_flag) {
        $level .= 'engr ';
    }
    if ($self->{user_levels}->{user} & $level_flag) {
        $level .= 'user';
    }
    return( $level );
}
######

###############################################################################
#
sub get_numeric_level {
    my( $self, $level_str ) = @_;

    my( @privs, $p, $numeric_level );

    $numeric_level = $self->{user_levels}{readonly};
    @privs = split(' ', $level_str);
    for $p (@privs) {
        if ($p eq 'user') {
            $numeric_level |= $self->{user_levels}{user};
        }
        elsif ($p eq 'engr') {
            $numeric_level |= $self->{user_levels}{engr};
        }
        elsif ($p eq 'admin') {
            $numeric_level |= $self->{user_levels}{admin};
        }
    }
    return( $numeric_level );
}
######

###############################################################################
#
sub check_authorization {
    my( $self, $user_priv, $required_privs ) = @_;

    my $error_msg = "";

    # first, see if user has been activated
    if ( $user_priv == $self->{user_levels}->{inactive} ) {
        $error_msg = "This account is not authorized or activated yet.";
    }
    # next, see if user has at least the required privileges
    elsif (!($user_priv & $required_privs)) {
        $error_msg = "This function requires the following privileges: " .
                     $self->get_str_level($required_privs);
    }
    return $error_msg;
}
######

###############################################################################
# process_registration
# In:  reference to hash of parameters
# Out: status message
#
sub process_registration {
    my( $self, $inref, @insertions ) = @_;

    my( $sth, $query );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    my $encrypted_password = $inref->{password_once};

    # get current date/time string in GMT
    my $current_date_time = $inref->{utc_seconds};
	
    # login name overlap check
    $query = "SELECT user_dn FROM users WHERE user_dn = ?";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    if ( $sth->rows > 0 ) {
        $sth->finish();
        $results->{error_msg} = "The selected login name is already taken " .
                   "by someone else; please choose a different login name.";
        return( 1, $results );
    }

    $sth->finish();

    $query = "INSERT INTO users VALUES ( " .
                              "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                            @insertions);
    if ( $results->{error_msg} ) { return( 1, $results ); }
    $sth->finish();

    $results->{status_msg} = "Your user registration has been recorded " .
        "successfully. Your login name is <strong>$user_dn</strong>. Once " .
        "your registration is accepted, information on " .
        "activating your account will be sent to your primary email address.";
    return( 0, $results );
}
######

##############################################
# Methods requiring administrative privileges.
##############################################

###############################################################################
# get_userlist
# In:  inref
# Out: status message and DB results
#
sub get_userlist
{
    my( $self, $inref, $fields_to_read ) = @_;
    my( $sth, $error_status, $query );
    my( %mapping, $r, $arrayref, $rref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->check_connection($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->check_authorization(
                                $self->get_numeric_level($inref->{user_level}),
                                $self->{user_levels}->{admin});
    if ($results->{error_msg}) { return( 1, $results ); }

    $query = "SELECT " . join(', ', @$fields_to_read);
    $query .= " FROM users ORDER BY user_last_name";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                              $query);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    # replace institution id with institution name
    $query = "SELECT institution_id, institution_name FROM institutions";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                              $query);
    if ( $results->{error_msg} ) { return( 1, $results ); }
    $arrayref = $sth->fetchall_arrayref();
    for $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    for $r (@$rref) {
        $r->{institution_id} = $mapping{$r->{institution_id}};
        # replace numeric user level code with string containing permissions
        $r->{user_level} = $self->get_level_description($r->{user_level});
    }


    $results->{rows} = $rref;
    $results->{status_msg} = 'Successfully read user list';
    return( 0, $results );
}
######

1;
