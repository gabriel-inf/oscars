package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with admin and user forms.
# Last modified: June 30, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;

use AAAS::Frontend::Database;
use AAAS::Frontend::Registration;
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
                       'database' => $self->{configs}->{use_AAAS_database},
                       'login' => $self->{configs}->{AAAS_login_name},
                       'password' => $self->{configs}->{AAAS_login_passwd})
                        or die "FATAL:  could not connect to database";
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

    my( $query, $sth, $ref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{auth}->get_user_levels('');
    if ($results->{error_msg}) { return( 1, $results); }

    # Get the password and privilege level from the database.
    $query = "SELECT user_password, user_level FROM users WHERE user_dn = ?";
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query('', $query,
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
        return (1, $results);
    }
    $sth->finish();

    # make sure has at least minimal privileges
    $results->{error_msg} = $self->{auth}->verify($ref->{user_level}, 'user');
    if ($results->{error_msg}) { return( 1, $results ); }

    # if everything is OK, create a db handle for the user, and set their
    # status to 'Logged in'
    $results->{error_msg} = $self->{dbconn}->login_user($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{user_level} = $self->{auth}->get_str_level($ref->{user_level});
    $results->{status_msg} = $user_dn . ' successfully logged in';
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

    if (!$inref->{admin_dn}) {
        $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    }
    else {
        $results->{error_msg} = $self->{dbconn}->enforce_connection(
                                                     $inref->{admin_dn});
    }
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->{auth}->verify($inref->{user_level},
                                                  'user', 1);
    if ($results->{error_msg}) { return( 1, $results ); }

    # DB query: get the user profile detail
    $query = "SELECT " . join(', ', @user_profile_fields) .
             " FROM users where user_dn = ?";

    if (!$inref->{admin_dn}) {
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn,
                                                             $query, $user_dn);
    }
    else {
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query(
                                         $inref->{admin_dn}, $query, $user_dn);
    }
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
    if (!$inref->{admin_dn}) {
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query(
                                                $user_dn, $query,
                                                @{$rref}[0]->{institution_id});
    }
    else {
        ($sth, $results->{error_msg}) = $self->{dbconn}->do_query(
                                                $inref->{admin_dn}, $query,
                                                @{$rref}[0]->{institution_id});
    }
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # check whether this organization is in the db
    if (!$sth->rows) {
        $sth->finish();
        $results->{error_msg} = 'No such organization recorded.';
        return (1, $results);
    }

    @data = $sth->fetchrow_array();
    $sth->finish();

    $results->{user_level} = $self->{auth}->get_str_level(
                                                       $results->{user_level});
    $results->{row} = @{$rref}[0];
    $results->{row}->{institution} = $data[0];
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
    my ( $self, $inref ) = @_;

    my( $sth, $query );
    my( $current_info, $ref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    if (!$inref->{admin_dn}) {
        $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    }
    else {
        $results->{error_msg} = $self->{dbconn}->enforce_connection(
                                                     $inref->{admin_dn});
    }
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->{auth}->verify($inref->{user_level},
                                                  'user', 1);
    if ($results->{error_msg}) { return( 1, $results ); }

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    $query = "SELECT " . join(', ', @user_profile_fields) .
             ", user_level FROM users where user_dn = ?";

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
    $results->{error_msg} = $self->{auth}->verify($current_info->{user_level},
                                                  'user');
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

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $inref->{password_new_once} ) {
        $inref->{user_password} = crypt( $inref->{password_new_once},
                                         'oscars');
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
    }

    # prepare the query for database update
    $query = "UPDATE users SET ";
    for $_ (@user_profile_fields) {
        $query .= "$_ = '$inref->{$_}', ";
        $ref->{$_} = $inref->{$_};
    }
    $query =~ s/,\s$//;
    $query .= " WHERE user_dn = ?";
    $sth->finish();

    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query,
                                                              $user_dn);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    $sth->finish();
    $ref->{institution} = $inref->{institution};
    $ref->{user_password} = undef;
    $results->{row} = $ref;
    $results->{status_msg} = "The account information for user " .
                          "$user_dn has been updated successfully.";
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
    my( $self, $inref ) = @_;
    my( $sth, $error_status, $query );
    my( %mapping, $r, $arrayref, $rref );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->{auth}->verify($inref->{user_level},
                                                  'admin', 1);
    if ($results->{error_msg}) { return( 1, $results ); }

    $query .= "SELECT * FROM users ORDER BY user_last_name";
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
        $r->{user_level} = $self->{auth}->get_str_level($r->{user_level});
    }


    $results->{rows} = $rref;
    $results->{status_msg} = 'Successfully read user list';
    return( 0, $results );
}
######

####################################
# Methods called from the BSS.
####################################

###############################################################################
# check_login_status
# In:  reference to hash of parameters
# Out: status code, status message
#
sub check_login_status {
    my( $self, $inref ) = @_;

    my $results = {};
    $results->{error_msg} = $self->{dbconn}->enforce_connection($inref->{user_dn});
    if ($results->{error_msg}) {
        return( 1, $results );
    }
    else {
        $results->{status_msg} = "User is logged in";
        return( 0, $results );
    }
}
######

1;
