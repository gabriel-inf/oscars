package AAAS::Frontend::Registration;

# Registration.pm:  Database interactions having to do with user registration.
#                   These methods haven't been fully incorporated yet.
# Last modified: November 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;
use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use AAAS::Frontend::Database;
use AAAS::Frontend::Auth;


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
}
######


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
        throw Common::Exception("Please check your login name and try again.");
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
        throw Common::Exception($non_match_error);
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
        throw Common::Exception("The selected login name is already taken " .
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

    print STDERR "add_user\n";
    my $encrypted_password = $inref->{password_once};

    # login name overlap check
    my $statement = "SELECT user_dn FROM users WHERE user_dn = ?";
    my $rows = $self->{dbconn}->do_query($statement, $user_dn);

    if ( scalar(@$rows) > 0 ) {
        throw Common::Exception("The login, $user_dn, is already taken " .
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

######
1;
