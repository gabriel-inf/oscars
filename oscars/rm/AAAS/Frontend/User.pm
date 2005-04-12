package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: April 12, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(process_user_login);

use AAAS::Frontend::Database;

# login:  login interaction with DB

# TODO:  FIX
$non_activated_user_level = -1;

##### sub process_user_login
# In: reference to hash of parameters
# Out: status code, status message
sub process_user_login
{
  my($loginname, $password) = @_;
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

  $error_code = database_lock_table($table{'users'});
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # get the password from the database
  $query = "SELECT $table_field{'users'}{'password'}, $table_field{'users'}{'level'} FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_unlock_table($table{'users'});
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $loginname );

  if ( $error_code ) {
      database_unlock_table($table{'users'});
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # check whether this person is a registered user
  my $password_matches = 0;
  if ( $num_of_affected_rows == 0 ) {
        # this login name is not in the database
      query_finish( $sth );
      database_unlock_table($table{'users'});
      database_disconnect( $dbh );
      return( 1, 'Please check your login name and try again.' );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq $non_activated_user_level ) {
                # this account is not authorized & activated yet
              database_disconnect( $dbh );
              database_unlock_table($table{'users'});
              return( 1, 'This account is not authorized or activated yet.' );
          }
          elsif ( $$ref[0] eq  $password ) {
              $password_matches = 1;
          }
      }
  }
  query_finish( $sth );
  database_unlock_table($table{'users'});

  if ( !$password_matches ) {
      database_disconnect( $dbh );
      return( 1, 'Please check your password and try again.' );
  }
  return( 0, 'The user has successfully logged in.' );
}


#### logout:  DB operations associated with logout

sub handle_logout
{
  my( $dbh, $sth, $error_code, $query );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) {
      return( 1, $error_code );
  }
    # TODO:  lock table, and redo to change status of user in user table
  query_finish( $sth );
    # TODO:  unlock the table(s)
  database_disconnect( $dbh );
  return (0, 'OK');
}


# myprofile:  Profile DB interaction

##### sub get_user_detail
# In: reference to hash of parameters
# Out: status code, status message
sub get_user_detail
{
  my($args_href) = @_;
  my( $dbh, $sth, $error_code, $query );

  ### get the user detail from the database and populate the profile form
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) {
      return( 1, $error_code );
  }

    # names of the fields to be displayed on the screen
  my @fields_to_display = ( 'last_name', 'first_name', 'dn', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_display ) {
      $query .= $table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, undef ) = query_execute( $sth, $args_href->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # populate %user_profile_data with the data fetched from the database
  my %user_profile_data;
  @user_profile_data{@fields_to_display} = ();
  $sth->bind_columns( map { \$user_profile_data{$_} } @fields_to_display );
  $sth->fetch();

  query_finish( $sth );
  database_disconnect( $dbh );
}


##### sub process_profile_update
# In: reference to hash of parameters
# Out: status code, status message
sub process_profile_update
{
  my ($args_href) = @_;
  my( $dbh, $sth, $error_code, $query );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) {
      return( 1, $error_code );
  }

    # user level provisioning:  # if the user's level equals one of the
    #  read-only levels, don't give them access 
  $query = "SELECT $table_field{'users'}{'level'} FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";

    # TODO:  lock necessary tables with LOCK_TABLE

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $args_href->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  while ( my $ref = $sth->fetchrow_arrayref ) {
      foreach $read_only_level ( @read_only_user_levels ) {
          if ( $$ref[0] eq $read_only_level ) {
              query_finish( $sth );
                  # TODO:  unlock table(s)
              database_disconnect( $dbh );
              return( 0, '[ERROR] Your user level (Lv. ' . $$ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.' );
          }
      }
  }

  query_finish( $sth );

    # Read the current user information from the database to decide which
    # fields are being updated.  'password' should always be the last entry
    #  of the array (important for later procedures)
  my @fields_to_read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'password' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      $query .= $table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }
  ( $error_code, undef ) = query_execute( $sth, $args_href->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # populate %user_profile_data with the data fetched from the database
  my %user_profile_data;
  @user_profile_data{@fields_to_read} = ();
  $sth->bind_columns( map { \$user_profile_data{$_} } @fields_to_read );
  $sth->fetch();

  query_finish( $sth );

    ### check the current password with the one in the database before
    ### proceeding
  if ( $user_profile_data{'password'} ne $args_href->{'password_current'} ) {
        # TODO:  unlock table(s)
      database_disconnect( $dbh );
      return( 1, 'Please check the current password and try again.' );
  }
      # TODO:  unlock table(s)

    # determine which fields to update in the user profile table
    # @fields_to_update and @values_to_update should be an exact match
    my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields/
    # values to update
  if ( $update_password ) {
      push( @fields_to_update, $table_field{'users'}{'password'} );
      push( @values_to_update, $encrypted_password );
  }

    # Remove password from the update comparison list.  'password' is the
    # last element of the array; remove it from the array
  $#fields_to_read--;

    # compare the current & newly input user profile data and determine
    # which fields/values to update
  foreach $_ ( @fields_to_read ) {
      if ( $user_profile_data{$_} ne $soap_args{$_} ) {
          push( @fields_to_update, $table_field{'users'}{$_} );
          push( @values_to_update, $soap_args{$_} );
      }
  }

    # if there is nothing to update...
  if ( $#fields_to_update < 0 ) {
      database_disconnect( $dbh );
      return( 1, 'There is no changed information to update.' );
  }

    # prepare the query for database update
  $query = "UPDATE $table{'users'} SET ";
  foreach $_ ( 0 .. $#fields_to_update ) {
      $query .= $fields_to_update[$_] . " = ?, ";
  }
  $query =~ s/,\s$//;
  $query .= " WHERE $table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, undef ) = query_execute( $sth, @values_to_update, $args_href->{'loginname'} );
  if ( $error_code ) {
          # TODO:  unlock table(s)
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error has occurred while updating your account information.<br>[Error] ' . $error_code );
  }

  query_finish( $sth );
        # TODO:  unlock table(s)
  database_disconnect( $dbh );
  return( 0, 'Your account information has been updated successfully.' );
}


# activateaccount:  Account Activation DB methods

##### sub process_account_activation
# In: reference to hash of parameters
# Out: status code, status message
sub process_account_activation
{
  my( $args_href ) = @_;
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return (1, $error_code ); }

    # get the password from the database
  $query = "SELECT $table_field{'users'}{'password'}, $table_field{'users'}{'activation_key'}, $table_field{'users'}{'pending_level'} FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return ( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $args_href->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return ( 1, $error_code );
  }

      # check whether this person is a registered user
  my $keys_match = 0;
  my( $pending_level, $non_match_error );

  if ( $num_of_affected_rows == 0 ) {
        # this login name is not in the database
      database_disconnect( $dbh );
      return ( 1, 'Please check your login name and try again.' );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq '' ) {
              $non_match_error = 'This account has already been activated.';
          }
          elsif ( $$ref[0] ne $args_href->{'password'} ) {
              $non_match_error = 'Please check your password and try again.';
          }
          elsif ( $$ref[1] ne $args_href->{'activation_key'} ) {
              $non_match_error = 'Please check the activation key and try again.';
          }
          else {
              $keys_match = 1;
              $pending_level = $$ref[2];
          }
      }
  }
  query_finish( $sth );

      ### if the input password and the activation key matched against those
      ### in the database, activate the account
  if ( $keys_match ) {
        # TODO:  lock necessary tables here
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
    $query = "UPDATE $table{'users'} SET $table_field{'users'}{'level'} = ?, $table_field{'users'}{'pending_level'} = ?, $table_field{'users'}{'activation_key'} = '' WHERE $table_field{'users'}{'dn'} = ?";

      ( $error_code, $sth ) = query_prepare( $dbh, $query );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }

      ( $error_code, undef ) = query_execute( $sth, $pending_level, '0', $args_href->{'loginname'} );
      if ( $error_code ) {
          database_disconnect( $dbh );
          return( 1, $error_code );
      }
          # TODO:  unlock the table(s)
      query_finish( $sth );
      database_disconnect( $dbh );
  }
  else {
      database_disconnect( $dbh );
      return( 1, $non_match_error );
  }
  return( 0, 'The user account <strong>' . $args_href->{'loginname'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.' );
}


# register:  user account registration db

##### sub process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
  my( $args_href ) = @_;
  my $encrypted_password = $args_href->{'password_once'};

    # get current date/time string in GMT
  my $current_date_time = $args_href ->{'utc_seconds'};
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );
	
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # login name overlap check
  $query = "SELECT $table_field{'users'}{'dn'} FROM $table{'users'} WHERE $table_field{'users'}{'dn'} = ?";
	# TODO:  lock table(s) with LOCK_TABLES
  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $args_href->{'loginname'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }
  query_finish( $sth );

  if ( $num_of_affected_rows > 0 ) {
      database_disconnect( $dbh );
        # TODO:  unlock table(s)
      return( 0, 'The selected login name is already taken by someone else; please choose a different login name.' );
  }

  $query = "INSERT INTO $table{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
    database_disconnect( $dbh );
    return( 1, $error_code );
  }

    # Initial user level is set to 0; needs admin accept/user activation to
    # raise the user level
  my @stuffs_to_insert = ( '', $args_href->{'loginname'}, $encrypted_password, $args_href->{'firstname'}, $args_href->{'lastname'}, $args_href->{'organization'}, $args_href->{'email_primary'}, $args_href->{'email_secondary'}, $args_href->{'phone_primary'}, $args_href->{'phone_secondary'}, $args_href->{'description'}, 0, $current_date_time, '', 0 );

  ( $error_code, undef ) = query_execute( $sth, @stuffs_to_insert );
  if ( $error_code ) {
          # TODO:  unlock tables
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $error_code );
  }
  query_finish( $sth );

	# TODO:  unlock table(s)
  database_disconnect( $dbh );
  return( 0, 'Your user registration has been recorded successfully. Your login name is <strong>' . $args_href->{'loginname'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.' );
}

1;
