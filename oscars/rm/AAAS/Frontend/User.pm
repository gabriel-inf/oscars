package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: May 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use DBI;

use AAAS::Frontend::Database;
use Data::Dumper;


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
    $self->{'inactive_user'} = 0;
    $self->{'admin_user'} = 10;
}
######################################################################

## Methods called by user forms.

##### method verify_login
# In: reference to hash of parameters
# Out: status code, status message
sub verify_login
{
    my( $self, $inref ) = @_;
    my( $query, $sth, %results );

        # at present, multiple users may share a connection; access level needs to be checked on each
        # method call
    $results{'error_msg'} = $self->{'dbconn'}->check_connection($inref);
    if ($results{'error_msg'}) { return( 1, %results); }

        # Get the password and privilege level from the database, making sure user
        # exists.
    $query = "SELECT user_password, user_level FROM users WHERE user_dn = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'Please check your login name and try again.';
        return (1, %results);
    }

        # This login name is in the database, do authorization checks.
    my $encoded_password = crypt($inref->{'user_password'}, 'oscars');
    my $ref = $sth->fetchrow_arrayref;
        # see if user has been activated
    if ( $$ref[1] eq $self->{'inactive_user'} ) {
        $results{'error_msg'} = 'This account is not authorized or activated yet.';
    }
        # if request is from admin login, make sure has admin privileges
    elsif ( $inref->{'admin_required'} and ($$ref[1] ne $self->{'admin_user'}) ) {
        $results{'error_msg'} = 'This account does not have administrative privileges.';
    }
        # compare passwords
    elsif ( $$ref[0] ne $encoded_password ) {
        $results{'error_msg'} = 'Please check your password and try again.';
    }
    $sth->finish();
    if ($results{'error_msg'}) { return( 1, %results ); }

    $results{'user_level'} = $$ref[1];
    $results{'status_msg'} = 'The user has successfully logged in.';
    # The first value is unused, but I can't get SOAP to send a correct
    # reply without it so far.
    return( 0, %results );
}


sub logout
{
    my( $self ) = @_;
    my( %results );

    if (!$self->{'dbconn'}->{'dbh'}) {
        $results{'status_msg'} = 'Already logged out.';
        return ( 0, %results );
    }
    if (!$self->{'dbconn'}->{'dbh'}->disconnect())
    {
        $results{'error_msg'} = "Could not disconnect from database";
        return ( 1, %results );
    }
    $self->{'dbconn'}->{'dbh'} = undef;
    $results{'status_msg'} = 'Logged out';
    return ( 0, %results );
}


##### method get_profile
# In: reference to hash of parameters
# Out: status code, status message
sub get_profile
{
    my( $self, $inref, $fields_to_read ) = @_;
    my( $sth, $query );
    my( @data, %results );

    $results{'error_msg'} = $self->{'dbconn'}->check_connection(undef);
    if ($results{'error_msg'}) { return( 1, %results); }

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @$fields_to_read ) {
        $query .= $_ . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE user_dn = ?";

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this person is a registered user
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'No such user.';
        return (1, %results);
    }

    # populate %results with the data fetched from the database
    @results{@$fields_to_read} = ();
    $sth->bind_columns( map { \$results{$_} } @$fields_to_read );
    $sth->fetch();
    $sth->finish();

    $query = "SELECT institution_name FROM institutions WHERE institution_id = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $results{'institution_id'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this organization is in the db
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'No such organization recorded.';
        return (1, %results);
    }

    @data = $sth->fetchrow_array();
    $results{'institution'} = $data[0];
    $sth->finish();

    $query = "SELECT user_level_description FROM user_levels WHERE user_level_enum = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $results{'user_level'});

    @data = $sth->fetchrow_array();
    $results{'level_description'} = $data[0];
    $sth->finish();
    #my($key, $value);
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
    my ( $self, $inref, $fields_to_read ) = @_;
    my( $sth, $query, $read_only_level, @read_only_user_levels );
    my( $current_info, $ref, $do_update, %results );

    $results{'error_msg'} = $self->{'dbconn'}->check_connection(undef);
    if ($results{'error_msg'}) { return( 1, %results); }

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    # TODO:  make sure user_level not in fields_to_read already
    $query = "SELECT user_level, ";
    foreach $_ ( @$fields_to_read ) {
        $query .= $_ . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE user_dn = ?";

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this person is a registered user
    if (!$sth->rows) {
        $results{'error_msg'} = "The user $inref->{'user_dn'} is not registered.";
        $sth->finish();
        return (1, %results);
    }

    # User level provisioning:  if the user's level equals one of the
    # read-only levels, don't give them access.
    $current_info = $sth->fetchrow_hashref;
    foreach $read_only_level ( @read_only_user_levels ) {
        if ( $current_info->{'user_level'} eq $read_only_level ) {
            $results{'error_msg'} = "Your user level ($current_info->{'user_level'}) has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.";
            $sth->finish();
            return ( 1, %results );
        }
    }

    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $current_info->{'user_password'} ne crypt($inref->{'user_password'},'oscars') ) {
        $results{'error_msg'} = 'Please check the current password and try again.';
        $sth->finish();
        return( 1, %results);
    }
    $sth->finish();

    $do_update = 0;

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $inref->{'password_new_once'} ) {
        $inref->{'user_password'} = crypt($inref->{'password_new_once'}, 'oscars');
        $do_update = 1;
    }
    else {
        $inref->{'user_password'} = crypt($inref->{'user_password'}, 'oscars');
    }

    # Check to see if the institution name provided is in the database.
    # If so, set the institution id to the primary key in the institutions
    # table.
    if ( $inref->{'institution'} ) {
        $query = "SELECT institution_id FROM institutions WHERE institution_name = ?";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'institution'});
        if ( $results{'error_msg'} ) { return( 1, %results ); }
        if (!$sth->rows) {
            $results{'error_msg'} = "The organization $inref->{'institution'} is not in the database.";
            $sth->finish();
            return (1, %results);
        }
        $ref = $sth->fetchrow_hashref;
        $inref->{'institution_id'} = $ref->{'institution_id'} ;
        $results{'institution'} = $inref->{'institution'}
    }

    # Compare the current & newly input user profile data and determine
    # which fields/values to update.  Assign results at this time also.
    foreach $_ ( @$fields_to_read ) {
        if ( $current_info->{$_} ne $inref->{$_} ) {
            $do_update = 1;
        }
        $results{$_} = $inref->{$_};
    }
    $results{'user_password'} = '';   # unset before passing back

    # if there is nothing to update...
    if ( !$do_update ) {
        $results{'error_msg'} = 'There is no changed information to update.';
        return( 1, %results );
    }

    # prepare the query for database update
    $query = "UPDATE users SET ";
    foreach $_ ( @$fields_to_read ) {
        if (defined($inref->{$_} )) {
            $query .= "$_ = '$inref->{$_}', ";
        }
    }
    $query =~ s/,\s$//;
    $query .= " WHERE user_dn = ?";

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $sth->finish();
    $results{'status_msg'} = "The account information for user $inref->{'user_dn'} has been updated successfully.";
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
    my( %results );

    $results{'error_msg'} = $self->{'dbconn'}->check_connection(undef);
    if ($results{'error_msg'}) { return( 1, %results); }

    # get the password from the database
    $query = "SELECT user_password, user_activation_key, user_level FROM users WHERE user_dn = ?";

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

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
        elsif ( $$ref[0] ne $inref->{'user_password'} ) {
            $non_match_error = 'Please check your password and try again.';
        }
        elsif ( $$ref[1] ne $inref->{'user_activation_key'} ) {
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
        $query = "UPDATE users SET user_level = ?, pending_level = ?, user_activation_key = '' WHERE user_dn = ?";
        ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $pending_level, $inref->{'user_dn'});
        if ( $results{'error_msg'} ) { return( 1, %results ); }
    }
    else {
        $sth->finish();
        results{'error_msg'} = $non_match_error;
        return( 1, %results );
    }
    $sth->finish();
    results{'status_msg'} = 'The user account <strong>' . $inref->{'user_dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
    return( 0, %results );
}


##### method process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
    my( $self, $inref, @insertions ) = @_;
    my( $sth, $query );
    my( %results );

    $results{'error_msg'} = $self->{'dbconn'}->check_connection(undef);
    if ($results{'error_msg'}) { return( 1, %results); }

    my $encrypted_password = $inref->{'password_once'};

    # get current date/time string in GMT
    my $current_date_time = $inref ->{'utc_seconds'};
	
    # login name overlap check
    $query = "SELECT user_dn FROM users WHERE user_dn = ?";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, $inref->{'user_dn'});
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    if ( $sth->rows > 0 ) {
        $sth->finish();
        results{'error_msg'} = 'The selected login name is already taken by someone else; please choose a different login name.';
        return( 1, %results );
    }

    $sth->finish();

    $query = "INSERT INTO users VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query, @insertions);
    if ( $results{'error_msg'} ) { return( 1, %results ); }
    $sth->finish();

    $results{'status_msg'} = 'Your user registration has been recorded successfully. Your login name is <strong>' . $inref->{'user_dn'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.';
    return( 0, %results );
}

## Called by Admin tool

##### method get_userlist
# In:  inref
# Out: status message and DB results
sub get_userlist
{
    my( $self, $inref, $fields_to_read ) = @_;
    my( $sth, $error_status, $query );
    my( %mapping, %results, $r, $arrayref, $rref );

    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    $query = "SELECT ";
    foreach $_ ( @$fields_to_read ) {
        $query .= $_ . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users ORDER BY user_last_name";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    if ( $results{'error_msg'} ) { return( 1, %results ); }

    $rref = $sth->fetchall_arrayref({user_last_name => 1, user_first_name => 2, user_dn => 3, user_email_primary => 4, user_level => 5, institution_id => 6 });
    $sth->finish();

        # replace institution id with institution name
    $query = "SELECT institution_id, institution_name FROM institutions";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    if ( $results{'error_msg'} ) { return( 1, %results ); }
    $arrayref = $sth->fetchall_arrayref();
    foreach $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    foreach $r (@$rref) {
        $r->{'institution_id'} = $mapping{$r->{'institution_id'}};
    }

        # replace numeric user level code with description
    $query = "SELECT user_level_enum, user_level_description FROM user_levels";
    ($sth, $results{'error_msg'}) = $self->{'dbconn'}->do_query($query);
    if ( $results{'error_msg'} ) { return( 1, %results ); }
    $arrayref = $sth->fetchall_arrayref();
    foreach $r (@$arrayref) { $mapping{$$r[0]} = $$r[1]; }

    foreach $r (@$rref) {
        $r->{'user_level'} = $mapping{$r->{'user_level'}};
    }

    $results{'rows'} = $rref;
    $results{'status_msg'} = 'Successfully read user list';
    return( 0, %results );
}

1;
