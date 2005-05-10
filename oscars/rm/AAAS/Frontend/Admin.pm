package AAAS::Frontend::Admin;

# Admin.pm:  database operations associated with administrative forms

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



#####

   # TODO:  find better home, where to set
our $admin_user_level = -1;
our $admin_dn = 'davidr';

# gateway:  DB operations having to do with logging in as admin

sub verify_acct
{
    ### Check whether admin account is set up in the database
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( %results );

    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # whether admin account exists (determine it with the level info, not the login name)
    $query = "SELECT $table{'users'}{'dn'} FROM users WHERE $table{'users'}{'user_level'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $admin_user_level );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While processing login: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is an administator
    if (!$sth->rows) {
        $sth->finish();
        $results{'error_msg'} = 'User does not have administrative privileges.';
        return (1, %results);
    }

    $sth->finish();
    $results{'status_msg'} = 'ID exists; going to admin login';
    # ID exists; go to admin login
    return( 0, %results );
}


##### method process_registration
# In: inref
# Out: None
sub process_registration
{
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( $encrypted_password );  # TODO:  FIX
    my( %results );
	
    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # insert into database query statement
    $query = "INSERT INTO users VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    # admin user level is set to 10 by default
    my @insertions = ( '', $admin_dn, $encrypted_password, $inref->{'firstname'}, $inref->{'lastname'}, $inref->{'organization'}, $inref->{'email_primary'}, $inref->{'email_secondary'}, $inref->{'phone_primary'}, $inref->{'phone_secondary'}, $inref->{'description'}, 10, $inref->{'datetime'}, '', 0 );

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( @insertions );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While recording admin account information. Please contact the webmaster for any inquiries.  $sth->errstr";
        return( 1, %results );
    }

    $sth->finish();
    $results{'status_msg'} = 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.';
    return( 0, %results );
}



##### method process_login
# In: inref
# Out: None
sub process_login
{
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( %results );

    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # get the password from the database
    $query = "SELECT $table{'users'}{'user_password'} FROM users WHERE $table{'users'}{'dn'} = ? and $table{'users'}{'user_level'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'}, $admin_user_level );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While processing administrative login: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person has a valid admin privilege
    if (!$sth->rows) {
        # this login name is not in the database or does not belong to the admin level
        $sth->finish();
        $results{'error_msg'} = 'Please check your login name and try again.';
        return (1, %results);
    }

    my $password_matches = 0;
    # this login name is in the database; compare passwords
    while ( my $ref = $sth->fetchrow_arrayref ) {
        if ( $$ref[0] eq $inref->{'password'} ) {
            $password_matches = 1;
        }
    }
    $sth->finish();
    $results{'status_msg'} = 'The admin user has successfully logged in.';
    return( 0, %results );
}


# adduser:  DB handling for adding a User page

##### method check_login_available
# In: hash reference to input arguments
# Out: status and results hash 
sub check_login_available
{
    my( $self, $inref ) = @_;
    my(  $sth, $query );
    my( %results );

    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # Check whether a particular user id already exists in the database
    # database table & field names to check
    my $field_name_to_check = $table{'users'}{'dn'};

    # query: select dn from users where dn='some_id_to_check';
    $query = "SELECT $field_name_to_check FROM users WHERE $field_name_to_check = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'id'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While checking whether login name available: $sth->errstr";
        return( 1, %results );
    }

    my $check_result;

    if ( $sth->rows == 0 ) {
        # ID does not overlap; usable
        $check_result = 'no';
    }
    else {
        # ID is already taken by someone else; unusable
        $check_result = 'yes';
    }
    $sth->finish();
    results{'status_msg'} = $check_result;
    return (0, %results);
}


##### method process_user_registration
# In: inref
# Out: None
sub process_user_registration
{
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( $encrypted_password, $activation_key );  # TODO:  FIX
    my( %results );
	
    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # id dup check.
    $query = "SELECT $table{'users'}{'dn'} FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While processing user registration: $sth->errstr";
        return( 1, %results );
    }
    # check whether this person is a registered user
    if ( $sth->rows > 0 ) {
        $sth->finish();
        results{'error_msg'} = 'The selected login name is already taken by someone else; please choose a different login name.';
        return (1, %results);
    }

    $sth->finish();

    # insert into database query statement
    # initial user level is preset by the admin; no need to wait for
    #  authorization
    my @insertions = ( '', $inref->{'dn'}, $encrypted_password, $inref->{'firstname'}, $inref->{'lastname'}, $inref->{'organization'}, $inref->{'email_primary'}, $inref->{'email_secondary'}, $inref->{'phone_primary'}, $inref->{'phone_secondary'}, $inref->{'description'}, 0, $inref->{'datetime'}, $activation_key, $inref->{'level'} );

    $query = "INSERT INTO users VALUES ( " . join( ', ', ('?') x @insertions ) . " )";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( @insertions );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While processing user registration: $sth->errstr";
        return( 1, %results );
    }

    $sth->finish();
    results{'status_msg'} = 'The new user account \'' . $inref->{'dn'} . '\' has been created. The user will receive an email informing that activation was successful.';
    return( 0, %results );
}


# editprofile:  DB handling for Edit Admin Profile page

##### sub get_user_profile
# In:  inref
# Out: status message and DB results
sub get_user_profile
{
    my( $self, $inref ) = @_;
    ### get the user detail from the database and populate the profile form
    my( $sth, $query );
    my( %results );

    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # names of the fields to be displayed on the screen
    my @fields_to_display = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description' );

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @fields_to_display ) {
        my $temp = 'user_' . $_;
        $query .= $table{'users'}{$temp} . ", ";
    }
    # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While getting user profile: $sth->errstr";
        return( 1, %results );
    }

    # populate %results with the data fetched from the database
    @results{@fields_to_display} = ();
    $sth->bind_columns( map { \$results{$_} } @fields_to_display );
    $sth->fetch();
    $sth->finish();

    $results{'status_msg'} = 'success';
    return ( 0, %results );
}


##### method process_profile_update
# In: inref
# Out: status message, results
sub process_profile_update
{
    my( $self, $inref ) = @_;
    my( $sth, $query );
    my( $temp );
    my( $update_password, $encrypted_password );
    my( %results );
	
    if (!($self->{'dbconn'}->{'dbh'})) { return( 1, "Database connection not valid\n"); }

    my( %table ) = get_AAAS_table();
    # Read the current user information from the database to decide which
    # fields are being updated.  'password' should always be the last entry
    # of the array (important for later procedures)
    my @fields_to_read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'password' );

    # DB query: get the user profile detail
    $query = "SELECT ";
    foreach $_ ( @fields_to_read ) {
        $temp = 'user_' . $_;
        $query .= $table{'users'}{$temp} . ", ";
    }
      # delete the last ", "
    $query =~ s/,\s$//;
    $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While updating user profile: $sth->errstr";
        return( 1, %results );
    }


    # populate %results with the data fetched from the database
    @results{@fields_to_read} = ();
    $sth->bind_columns( map { \$results{$_} } @fields_to_read );
    $sth->fetch();

    ### check the current password with the one in the database before
    ### proceeding
    if ( $results{'password'} ne $inref->{'password_current'} ) {
        $sth->finish();
        $results{'error_msg'} = 'Please check the current password and try again.';
        return ( 1, %results );
    }

    # determine which fields to update in the user profile table
    # @fields_to_update and @values_to_update should be an exact match
    my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields
    #  /values to update
    if ( $update_password ) {
        push( @fields_to_update, $table{'users'}{'user_password'} );
        push( @values_to_update, $encrypted_password );
    }

    # remove password from the update comparison list
    # 'password' is the last element of the array; remove it from the array
    $#fields_to_read--;

    # compare the current & newly input user profile data and determine which fields/values to update
    foreach $_ ( @fields_to_read ) {
        if ( $results{$_} ne $inref->{$_} ) {
            $temp = 'user_' . $_;
            push( @fields_to_update, $table{'users'}{$temp} );
            push( @values_to_update, $inref->{$_} );
        }
    }

    # if there is nothing to update...
    if ( $#fields_to_update < 0 ) {
        $sth->finish();
        results{'error_msg'} = 'There is no changed information to update.';
        return( 1, %results );
    }
    $sth->finish();

    # prepare the query for database update
    $query = "UPDATE users SET ";
    foreach $_ ( 0 .. $#fields_to_update ) {
        $query .= $fields_to_update[$_] . " = ?, ";
    }
    $query =~ s/,\s$//;
    $query .= " WHERE $table{'users'}{'dn'} = ?";

    $sth = $self->{'dbconn'}->{'dbh'}->prepare( $query );
    if (!$sth) {
        $results{'error_msg'} = "Can't prepare statement\n" . $self->{'dbconn'}->{'dbh'}->errstr;
        return (1, %results);
    }
    $sth->execute( @values_to_update, $inref->{'dn'} );
    if ( $sth->errstr ) {
        $sth->finish();
        $results{'error_msg'} = "[ERROR] While updating account information: $sth->errstr";
        return( 1, %results );
    }
    $sth->finish();
    $results{'status_msg'} = 'The account information has been updated successfully.';
    return( 0, %results );
}

1;
