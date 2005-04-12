package AAAS::Frontend::Admin;

# Admin.pm:  database operations associated with administrative forms

# Last modified: April 12, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use AAAS::Frontend::Database;


# gateway:  DB operations having to do with logging in as admin

sub verify_acct
{
    ### Check whether admin account is set up in the database
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # whether admin account exists (determine it with the level info, not the login name)
  $query = "SELECT $table_field{'users'}{'user_loginname'} FROM $table{'users'} WHERE $table_field{'users'}{'user_level'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $admin_user_level );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  query_finish( $sth );
  database_disconnect( $dbh );

    ### proceed to the appropriate next action depending on the existence of
    ###  the admin account
  if ( $num_of_affected_rows == 0 ) {
        # ID does not exist; go to admin registration
      return();
  }
    # ID exists; go to admin login
  return();
}


##### sub process_registration
# In: args_href
# Out: None
sub process_registration
{
  my( $dbh, $sth, $error_code, $query );
  undef $error_code;
	
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

      # TODO:  lock table
      # insert into database query statement
  $query = "INSERT INTO $table{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

    # admin user level is set to 10 by default
  my @stuffs_to_insert = ( '', $admin_loginname_string, $encrypted_password, $args_href->{'firstname'}, $args_href->{'lastname'}, $args_href->{'organization'}, $args_href->{'email_primary'}, $args_href->{'email_secondary'}, $args_href->{'phone_primary'}, $args_href->{'phone_secondary'}, $args_href->{'description'}, 10, $args_href->{'datetime'}, '', 0 );

  ( $error_code, undef ) = query_execute( $sth, @stuffs_to_insert );
                # TODO:  release db lock
  if ( $error_code ) {
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error occurred recording the admin account information on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $error_code );
  }

  query_finish( $sth );
	# TODO:  unlock the operation
  database_disconnect( $dbh );

  return( 0, 'Admin account registration has been completed successfully.<br>Please <a href="gateway.pl">click here</a> to proceed to the Admin Tool login page.' );

}


##### sub process_login
# In: args_href
# Out: None
sub process_login
{
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # get the password from the database
  $query = "SELECT $table_field{'users'}{'user_password'} FROM $table{'users'} WHERE $table_field{'users'}{'user_loginname'} = ? and $table_field{'users'}{'user_level'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $args_href->{'loginname'}, $admin_user_level );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }
    # check whether this person has a valid admin privilege
  my $password_matches = 0;
  if ( $num_of_affected_rows == 0 ) {
        # this login name is not in the database or does not belong to the admin level
      return( 1, 'Please check your login name and try again.' );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[0] eq $args_href->{'password'} ) {
              $password_matches = 1;
          }
      }
  }
  query_finish( $sth );
  return( 0, 'The admin user has successfully logged in.' );
}


# adduser:  DB handling for adding a User page

##### sub check_login_available
# In: args_href
# Out: $check_result [yes(overlaps)/no(doesn't overlap)]
sub check_login_available
{
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # Check whether a particular user id already exists in the database
    # database table & field names to check
  my $table_name_to_check = $table{'users'};
  my $field_name_to_check = $table_field{'users'}{'user_loginname'};

      # query: select user_loginname from users where user_loginname='some_id_to_check';
  $query = "SELECT $field_name_to_check FROM $table_name_to_check WHERE $field_name_to_check = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, $num_of_affected_rows ) = query_execute( $sth, $args_href->{'id'} );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  my $check_result;

  if ( $num_of_affected_rows == 0 ) {
        # ID does not overlap; usable
      $check_result = 'no';
  }
  else {
        # ID is already taken by someone else; unusable
      $check_result = 'yes';
  }
  query_finish( $sth );
  database_disconnect( $dbh );
  return (0, $check_result);
}


##### sub process_user_registration
# In: args_href
# Out: None
sub process_user_registration
{
  my( $dbh, $sth, $error_code, $query, $num_of_affected_rows );
  undef $error_code;
	
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # id overlap check.  We're not using the pre-existing sub routine here to
    # perform the task within a single, locked database connection
  $query = "SELECT $table_field{'users'}{'user_loginname'} FROM $table{'users'} WHERE $table_field{'users'}{'user_loginname'} = ?";

	# TODO:  lock  database operations
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
      # TODO:  release lock
  if ( $num_of_affected_rows > 0 ) {
      database_disconnect( $dbh );
      return( 1, 'The selected login name is already taken by someone else; please choose a different login name.' );
  }
    # insert into database query statement
    # initial user level is preset by the admin; no need to wait for
    #  authorization
  my @stuffs_to_insert = ( '', $args_href->{'loginname'}, $encrypted_password, $args_href->{'firstname'}, $args_href->{'lastname'}, $args_href->{'organization'}, $args_href->{'email_primary'}, $args_href->{'email_secondary'}, $args_href->{'phone_primary'}, $args_href->{'phone_secondary'}, $args_href->{'description'}, 0, $args_href->{'datetime'}, $activation_key, $args_href->{'level'} );

  $query = "INSERT INTO $table{'users'} VALUES ( " . join( ', ', ('?') x @stuffs_to_insert ) . " )";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
        # TODO:  release lock
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( undef, $error_code ) = query_execute( $sth, @stuffs_to_insert );
  if ( $error_code ) {
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error has occurred while recording the account registration on the database.<br>[Error] ' . $error_code );
  }

  query_finish( $sth );
	# TODO:  unlock the operation
  database_disconnect( $dbh );
  return( 0, 'The new user account \'' . $args_href->{'loginname'} . '\' has been created successfully. <br>The user will receive information on activating the account in email shortly.' );

}


# editprofile:  DB handling for Edit Admin Profile page

##### sub get_user_profile
# In:  args_href
# Out: status message and DB results
sub get_user_profile
{
    ### get the user detail from the database and populate the profile form
  my( $dbh, $sth, $error_code, $query );

  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

    # names of the fields to be displayed on the screen
  my @fields_to_display = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_display ) {
      my $temp = 'user_' . $_;
      $query .= $table_field{'users'}{$temp} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $table{'users'} WHERE $table_field{'users'}{'user_loginname'} = ?";

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

  foreach $field ( @fields_to_display ) {
      $Html_Line =~ s/(name="$field")/$1 value="$user_profile_data{$field}"/i;
  }
  return (0, 'success');
}


##### sub process_profile_update
# In: args_href
# Out: status message, results
sub process_profile_update
{
  my( $dbh, $sth, $error_code, $query );
  undef $error_code;
	
  ( $error_code, $dbh ) = database_connect();
  if ( $error_code ) { return( 1, $error_code ); }

      # TODO:  lock with LOCK_TABLE
      # Read the current user information from the database to decide which
      # fields are being updated.  'password' should always be the last entry
      # of the array (important for later procedures)
  my @fields_to_read = ( 'firstname', 'lastname', 'organization', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'password' );

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      my $temp = 'user_' . $_;
      $query .= $table_field{'users'}{$temp} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $table{'users'} WHERE $table_field{'users'}{'user_loginname'} = ?";

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
  if ( $user_profile_data{'password'} ne &Encode_Passwd( $args_href->{'password_current'} ) ) {
                # TODO:  release lock
      database_disconnect( $dbh );
      return( 1, 'Please check the current password and try again.' );
  }
	# determine which fields to update in the user profile table
	# @fields_to_update and @values_to_update should be an exact match
  my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields
    #  /values to update
  if ( $update_password ) {
      push( @fields_to_update, $table_field{'users'}{'user_password'} );
      push( @values_to_update, $encrypted_password );
  }

    # remove password from the update comparison list
    # 'password' is the last element of the array; remove it from the array
  $#fields_to_read--;

    # compare the current & newly input user profile data and determine which fields/values to update
  foreach $_ ( @fields_to_read ) {
      if ( $user_profile_data{$_} ne $args_href->{$_} ) {
          my $temp = 'user_' . $_;
          push( @fields_to_update, $table_field{'users'}{$temp} );
          push( @values_to_update, $args_href->{$_} );
      }
  }

    # if there is nothing to update...
  if ( $#fields_to_update < 0 ) {
          # TODO:  release lock
      database_disconnect( $dbh );
      return( 1, 'There is no changed information to update.' );
  }

    # prepare the query for database update
  $query = "UPDATE $table{'users'} SET ";
  foreach $_ ( 0 .. $#fields_to_update ) {
      $query .= $fields_to_update[$_] . " = ?, ";
  }
  $query =~ s/,\s$//;
  $query .= " WHERE $table_field{'users'}{'user_loginname'} = ?";

  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( 1, $error_code );
  }

  ( $error_code, undef ) = query_execute( $sth, @values_to_update, $args_href->{'loginname'} );
  if ( $error_code ) {
                # TODO:  release lock
      database_disconnect( $dbh );
      $error_code =~ s/CantExecuteQuery\n//;
      return( 1, 'An error has occurred while updating the account information.<br>[Error] ' . $error_code );
  }

  query_finish( $sth );
	# TODO:  unlock table
  database_disconnect( $dbh );
  return( 0, 'The account information has been updated successfully.' );
}


# logout:   DB operations associated with admin logout

sub handle_logout
{
    ( $error_code, $dbh ) = database_connect();
    if ( $error_code ) { return( 1, $error_code ); }

    #query_finish( $sth );

    database_disconnect( $dbh );
    return ( 1, 'not done yet' );
}

1;
