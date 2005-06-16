package AAAS::Frontend::Registration;

# Registration.pm:  Database interactions having to do with user registration.
#                   These methods haven't been fully incorporated yet.
# Last modified: June 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;

use AAAS::Frontend::Database;
use AAAS::Frontend::Auth;


###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($self) = {%_args};
  
    # Bless $self into designated class.
    bless($self, $_class);
  
    # Initialize.
    $self->initialize();
  
    return($self);
}

sub initialize {
    my ($self) = @_;
}
######


###############################################################################
# activate_account
# In:  reference to hash of parameters
# Out: status code, status message
#
sub activate_account {
    my( $self, $inref, $required_level ) = @_;

    my( $sth, $query );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->enforce_connx($user_dn, 0, 0);
    if ($results->{error_msg}) { return( 1, $results); }

    $results->{error_msg} = $self->{auth}->verify($inref->{user_level},
                                                  $required_level, 1);
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
# process_registration
# In:  reference to hash of parameters
# Out: status message
#
sub process_registration {
    my( $self, $inref, @insertions ) = @_;

    my( $sth, $query );
    my $results = {};
    my $user_dn = $inref->{user_dn};

    $results->{error_msg} = $self->{dbconn}->enforce_connx($user_dn, 0, 0);
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

1;
