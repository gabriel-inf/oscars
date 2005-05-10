package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: April 28, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use AAAS::Frontend::Database;

######################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

######################################################################
sub initialize {
    my ($self) = @_;
    $self->{'dbconn'} = AAAS::Frontend::Database->new('configs' => $self->{'configs'})
            or die "FATAL:  could not connect to database";
}
######################################################################


# TODO:  FIX
our $non_activated_user_level = -1;

# from login.pl:  login interaction with DB

##### method verify_login
# In: reference to hash of parameters
# Out: status code, status message
sub verify_login
{
    my( $self, $inref ) = @_;
    my( $query, $sth, %results );

    if (!defined($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = $self->{'dbconn'}->get_AAAS_table('users');
    # get the password from the database
    $query = "SELECT $table{'users'}{'password'}, $table{'users'}{'level'} FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While processing the login: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'Please check your login name and try again.';
        return (1, %results);
    }
        # this login name is in the database; compare passwords
    my $password_matches = 0;
    while ( my $ref = $sth->fetchrow_arrayref ) {
        if ( $$ref[1] eq $non_activated_user_level ) {
            # this account is not authorized & activated yet
            $sth->finish();
            $results{'error_msg'} = 'This account is not authorized or activated yet.';
            return( 1, %results );
        }
        elsif ( $$ref[0] eq  $inref->{'password'} ) {
            $password_matches = 1;
        }
    }
    $sth->finish();

    if ( !$password_matches ) {
        $results{'error_msg'} = 'Please check your password and try again.';
    }
    else {
        $results{'status_msg'} = 'The user has successfully logged in.';
    }
    # The first value is unused, but I can't get SOAP to send a correct
    # reply without it so far.
    return( 0, %results );
}


#### from logout.pl:  DB operations associated with logout, a noop right now

sub logout
{
    my( $self ) = @_;
    my( %results );

    results{'status_msg'} = 'Logged out';
    return ( 0, %results );
}


# from myprofile.pl:  Profile DB interaction

##### method get_profile
# In: reference to hash of parameters
# Out: status code, status message
sub get_profile
{
    my( $self, $inref, $fields_to_display ) = @_;
    my( $sth, $query );
    my( %results );
    my( %table ) = $self->{'dbconn'}->get_AAAS_table('users');

    if (!defined($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @$fields_to_display ) {
        $query .= $table{'users'}{$_} . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While getting the user profile: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'No such user.';
        return (1, %results);
    }

    # populate %results with the data fetched from the database
    @results{@$fields_to_display} = ();
    $sth->bind_columns( map { \$results{$_} } @$fields_to_display );
    $sth->fetch();
    $sth->finish();

    $query = "SELECT institution_name FROM institutions WHERE institution_id = ?";
    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $results{'institution_id'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While getting the user profile: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'No such organization recorded.';
        return (1, %results);
    }

    # flatten it out
    while (my @data = $sth->fetchrow_array()) {
        $results{'institution'} = $data[0];
    }   

    $sth->finish();
    my($key, $value);
    #foreach $key(sort keys %results)
    #{
          #$value = $results{$key};
    #}
    $results{'status_msg'} = 'Retrieved user profile';
    return ( 0, %results );
}


##### method set_profile
# In: reference to hash of parameters
# Out: status code, status message
sub set_profile
{
    my ( $self, $inref, @fields_to_read ) = @_;
    my( $sth, $query, $read_only_level, @read_only_user_levels );
    my( $update_password, $encrypted_password );   # TODO:  FIX
    my( %table ) = get_AAAS_table();
    my( %results );

    if (!defined($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    # user level provisioning:  # if the user's level equals one of the
    #  read-only levels, don't give them access 
    $query = "SELECT $table{'users'}{'level'} FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While updating your account profile: $sth->errstr";
        return( 1, %results );
    }

    while ( my $ref = $sth->fetchrow_arrayref ) {
        foreach $read_only_level ( @read_only_user_levels ) {
            if ( $$ref[0] eq $read_only_level ) {
                $sth->finish();
                $results{'error_msg'} = '[ERROR] Your user level (Lv. ' . $$ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.';
                return ( 1, %results );
            }
        }
    }
    $sth->finish();

    # Read the current user information from the database to decide which
    # fields are being updated.

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @fields_to_read ) {
        $query .= $table{'users'}{$_} . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While updating your account profile:  $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'No such person registered.';
        return (1, %results);
    }

    # populate %results with the data fetched from the database
    @results{@fields_to_read} = ();
    $sth->bind_columns( map { \$results{$_} } @fields_to_read );
    $sth->fetch();

    ### check the current password with the one in the database before
    ### proceeding
    if ( $results{'password'} ne $inref->{'password_current'} ) {
        $sth->finish();
        return( 1, 'Please check the current password and try again.' );
    }
    $sth->finish();

    # determine which fields to update in the user profile table
    # @fields_to_update and @values_to_update should be an exact match
    my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields/
    # values to update
    if ( $update_password ) {
        push( @fields_to_update, $table{'users'}{'password'} );
        push( @values_to_update, $encrypted_password );
    }

    # Remove password from the update comparison list.  'password' is the
    # last element of the array; remove it from the array
    $#fields_to_read--;

    # compare the current & newly input user profile data and determine
    # which fields/values to update
    foreach $_ ( @fields_to_read ) {
        if ( $results{$_} ne $inref->{$_} ) {
            push( @fields_to_update, $table{'users'}{$_} );
            push( @values_to_update, $inref->{$_} );
        }
    }

    # if there is nothing to update...
    if ( $#fields_to_update < 0 ) {
        return( 1, 'There is no changed information to update.' );
    }

    # prepare the query for database update
    $query = "UPDATE users SET ";
    foreach $_ ( 0 .. $#fields_to_update ) {
        $query .= $fields_to_update[$_] . " = ?, ";
    }
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( @values_to_update, $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While updating your account information: $sth->errstr";
        return( 1, %results );
    }
    $sth->finish();
    $results{'status_msg'} = 'Your account information has been updated successfully.';
    return( 0, %results );
}


# activateaccount:  Account Activation DB methods

##### method activate_account
# In: reference to hash of parameters
# Out: status code, status message
sub activate_account
{
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( %table ) = get_AAAS_table();
    my( %results );

    if (!defined($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    # get the password from the database
    $query = "SELECT $table{'users'}{'password'}, $table{'users'}{'activation_key'}, $table{'users'}{'pending_level'} FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While activating your account: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'Please check your login name and try again.';
        return (1, %results);
    }

    my $keys_match = 0;
    my( $pending_level, $non_match_error );
        # this login name is in the database; compare passwords
    while ( my $ref = $sth->fetchrow_arrayref ) {
        if ( $$ref[1] eq '' ) {
            $non_match_error = 'This account has already been activated.';
        }
        elsif ( $$ref[0] ne $inref->{'password'} ) {
            $non_match_error = 'Please check your password and try again.';
        }
        elsif ( $$ref[1] ne $inref->{'activation_key'} ) {
            $non_match_error = 'Please check the activation key and try again.';
        }
        else {
            $keys_match = 1;
            $pending_level = $$ref[2];
        }
    }
    $sth->finish();

    ### if the input password and the activation key matched against those
    ### in the database, activate the account
    if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $query = "UPDATE users SET $table{'users'}{'level'} = ?, $table{'users'}{'pending_level'} = ?, $table{'users'}{'activation_key'} = '' WHERE $table{'users'}{'dn'} = ?";

        $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
        if (!defined($sth)) {
            $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
            return (1, %results);
        }
        $sth->execute( $pending_level, $inref->{'dn'} );
        if ( $sth->errstr ) {
            $sth->finish();
            $results{'error_msg'} = "[ERROR] While updating your account information: $sth->errstr";
            return( 1, %results );
        }
    }
    else {
        $sth->finish();
        results{'error_msg'} = $non_match_error;
        return( 1, %results );
    }
    $sth->finish();
    results{'status_msg'} = 'The user account <strong>' . $inref->{'dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
    return( 0, %results );
}


# register:  user account registration db

##### method process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
    my( $self, $inref, @insertions ) = @_;
    my( $sth, $query );
    my( %results );

    if (!defined($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    my $encrypted_password = $inref->{'password_once'};

    # get current date/time string in GMT
    my $current_date_time = $inref ->{'utc_seconds'};
	
    # login name overlap check
    $query = "SELECT $table{'users'}{'dn'} FROM users WHERE $table{'users'}{'dn'} = ?";
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    if ( $sth->rows > 0 ) {
        $sth->finish();
        results{'error_msg'} = 'The selected login name is already taken by someone else; please choose a different login name.';
        return( 1, %results );
    }

    $sth->finish();

    $query = "INSERT INTO users VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    $sth = $self->{'dbconn'}{'dbh'}->prepare( $query );
    if (!defined($sth)) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( @insertions );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While recording your registration. Please contact the webmaster for any inquiries. $sth->errstr";
        return( 1, %results );
    }
    $sth->finish();

    $results{'status_msg'} = 'Your user registration has been recorded successfully. Your login name is <strong>' . $inref->{'dn'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.';
    return( 0, %results );
}

1;
