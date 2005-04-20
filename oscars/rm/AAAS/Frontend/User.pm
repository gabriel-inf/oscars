package AAAS::Frontend::User;

# User.pm:  Database interactions having to do with user forms.
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

use lib '../..';

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(verify_login get_profile set_profile );

use DB;
use AAAS::Frontend::Database;

# TODO:  FIX
our $non_activated_user_level = -1;

# from login.pl:  login interaction with DB

##### sub verify_login
# In: reference to hash of parameters
# Out: status code, status message
sub verify_login
{
  my($inputs) = @_;
  my( $dbh, $sth, $query, %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # get the password from the database
  $query = "SELECT $Table_field{'users'}{'password'}, $Table_field{'users'}{'level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this person is a registered user
  my $password_matches = 0;
  if ( $sth->rows == 0 ) {
        # this login name is not in the database
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} = 'Please check your login name and try again.';
      return( 1, %results );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq $non_activated_user_level ) {
                # this account is not authorized & activated yet
              db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
              $results{'error_msg'} = 'This account is not authorized or activated yet.';
              return( 1, %results );
          }
          elsif ( $$ref[0] eq  $inputs->{'password'} ) {
              $password_matches = 1;
          }
      }
  }
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});

  if ( !$password_matches ) {
      $results{'error_msg'} = 'Please check your password and try again.';
  }
  $results{'status_msg'} = 'The user has successfully logged in.';
      # The first value is unused, but I can't get SOAP to send a correct
      # reply without it so far.
  return( 0, %results );
}


#### from logout.pl:  DB operations associated with logout, a noop right now

sub logout
{
  my( $dbh, $sth, $query, %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) {
      return( 1, %results );
  }
  database_disconnect( $dbh );
  results{'status_msg'} = 'Logged out';
  return ( 0, %results );
}


# from myprofile.pl:  Profile DB interaction

##### sub get_profile
# In: reference to hash of parameters
# Out: status code, status message
sub get_profile
{
  my( $inputs, $fields_to_display ) = @_;
  my( $dbh, $sth, $query );
  my( %results );

  ### get the user detail from the database and populate the profile form
  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @$fields_to_display ) {
      $query .= $Table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return( 1, %results ) };

  if ( $sth->rows == 0 )
  {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} = 'No such user in the database';
      return( 1, %results );
  }

    # populate %results with the data fetched from the database
  @results{@$fields_to_display} = ();
  $sth->bind_columns( map { \$results{$_} } @$fields_to_display );
  $sth->fetch();
  query_finish( $sth );

  $query = "SELECT institution_name FROM $Table{'institutions'} WHERE institution_id = ?";
  ( $results{'error_msg'}, $sth ) = db_handle_query($dbh, $query, $Table{'institutions'}, READ_LOCK, $results{'institution_id'});
  if ( $results{'error_msg'} ) { return( 1, %results ) };

  if ( $sth->rows == 0 )
  {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      $results{'error_msg'} = 'No such organization in the database';
      return( 1, %results );
  }
    # flatten it out
  while (my @data = $sth->fetchrow_array()) {
         $results{'institution'} = $data[0];
  }   


  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
  my($key, $value);
  #foreach $key(sort keys %results)
  #{
        #$value = $results{$key};
  #}
  $results{'status_msg'} = 'Retrieved user profile';
  return ( 0, %results );
}


##### sub set_profile
# In: reference to hash of parameters
# Out: status code, status message
sub set_profile
{
  my ($inputs, @fields_to_read) = @_;
  my( $dbh, $sth, $query, $read_only_level, @read_only_user_levels );
  my( $update_password, $encrypted_password );   # TODO:  FIX
  my( %results );

  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) {
      return( 1, %results );
  }

    # user level provisioning:  # if the user's level equals one of the
    #  read-only levels, don't give them access 
  $query = "SELECT $Table_field{'users'}{'level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

  while ( my $ref = $sth->fetchrow_arrayref ) {
      foreach $read_only_level ( @read_only_user_levels ) {
          if ( $$ref[0] eq $read_only_level ) {
              db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
              $results{'error_msg'} = '[ERROR] Your user level (Lv. ' . $$ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.';
              return ( 1, %results );
          }
      }
  }
  query_finish( $sth );

    # Read the current user information from the database to decide which
    # fields are being updated.

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @fields_to_read ) {
      $query .= $Table_field{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

    # populate %results with the data fetched from the database
  @results{@fields_to_read} = ();
  $sth->bind_columns( map { \$results{$_} } @fields_to_read );
  $sth->fetch();

    ### check the current password with the one in the database before
    ### proceeding
  if ( $results{'password'} ne $inputs->{'password_current'} ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      return( 1, 'Please check the current password and try again.' );
  }
  query_finish( $sth );

    # determine which fields to update in the user profile table
    # @fields_to_update and @values_to_update should be an exact match
    my( @fields_to_update, @values_to_update );

    # if the password needs to be updated, add the new one to the fields/
    # values to update
  if ( $update_password ) {
      push( @fields_to_update, $Table_field{'users'}{'password'} );
      push( @values_to_update, $encrypted_password );
  }

    # Remove password from the update comparison list.  'password' is the
    # last element of the array; remove it from the array
  $#fields_to_read--;

    # compare the current & newly input user profile data and determine
    # which fields/values to update
  foreach $_ ( @fields_to_read ) {
      if ( $results{$_} ne $inputs->{$_} ) {
          push( @fields_to_update, $Table_field{'users'}{$_} );
          push( @values_to_update, $inputs->{$_} );
      }
  }

    # if there is nothing to update...
  if ( $#fields_to_update < 0 ) {
      database_unlock_table( $Table{'users'} );
      database_disconnect( $dbh );
      return( 1, 'There is no changed information to update.' );
  }

    # prepare the query for database update
  $query = "UPDATE $Table{'users'} SET ";
  foreach $_ ( 0 .. $#fields_to_update ) {
      $query .= $fields_to_update[$_] . " = ?, ";
  }
  $query =~ s/,\s$//;
  $query .= " FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, @values_to_update, $inputs->{'dn'} );
  if ( $results{'error_msg'}) {
      $results{'error_msg'} =~ s/CantExecuteQuery\n//;
      $results{'error_msg'} =  'An error has occurred while updating your account information.<br>[Error] ' . $results{'error_msg'};
      return( 1, %results );
  }

  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
  $results{'status_msg'} = 'Your account information has been updated successfully.';
  return( 0, %results );
}


# activateaccount:  Account Activation DB methods

##### sub process_account_activation
# In: reference to hash of parameters
# Out: status code, status message
sub process_account_activation
{
  my( $inputs ) = @_;
  my( $dbh, $sth, $query );
  my( %results );

  ($results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return (1, $results{'error_msg'} ); }

    # get the password from the database
  $query = "SELECT $Table_field{'users'}{'password'}, $Table_field{'users'}{'activation_key'}, $Table_field{'users'}{'pending_level'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return ( 1, %results ); }

      # check whether this person is a registered user
  my $keys_match = 0;
  my( $pending_level, $non_match_error );

  if ( $sth->rows == 0 ) {
        # this login name is not in the database
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      results{'error_msg'} =  'Please check your login name and try again.';
      return ( 1, %results );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq '' ) {
              $non_match_error = 'This account has already been activated.';
          }
          elsif ( $$ref[0] ne $inputs->{'password'} ) {
              $non_match_error = 'Please check your password and try again.';
          }
          elsif ( $$ref[1] ne $inputs->{'activation_key'} ) {
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
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
      $query = "UPDATE $Table{'users'} SET $Table_field{'users'}{'level'} = ?, $Table_field{'users'}{'pending_level'} = ?, $Table_field{'users'}{'activation_key'} = '' WHERE $Table_field{'users'}{'dn'} = ?";

      ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $pending_level, $inputs->{'dn'});
      if ( $results{'error_msg'} ) { return( 1, %results ); }
  }
  else {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      results{'error_msg'} = $non_match_error;
      return( 1, %results );
  }
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
  results{'status_msg'} = 'The user account <strong>' . $inputs->{'dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
  return( 0, %results );
}


# register:  user account registration db

##### sub process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
  my( $inputs, @insertions ) = @_;
  my( $dbh, $sth, $query );
  my( %results );

  my $encrypted_password = $inputs->{'password_once'};

    # get current date/time string in GMT
  my $current_date_time = $inputs ->{'utc_seconds'};
	
  ( $results{'error_msg'}, $dbh ) = database_connect($Dbname);
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # login name overlap check
  $query = "SELECT $Table_field{'users'}{'dn'} FROM $Table{'users'} WHERE $Table_field{'users'}{'dn'} = ?";
  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

  if ( $sth->rows > 0 ) {
      db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});
      results{'error_msg'} = 'The selected login name is already taken by someone else; please choose a different login name.';
      return( 1, %results );
  }

  query_finish( $sth );

  $query = "INSERT INTO $Table{'users'} VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

  ( $results{'error_msg'}, $sth) = db_handle_query($dbh, $query, $Table{'users'}, READ_LOCK, @insertions);
  if ( $results{'error_msg'} ) {
      $results{'error_msg'} =~ s/CantExecuteQuery\n//;
      $results{'error_msg'} = 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $results{'error_msg'};
      return( 1, %results );
  }
  db_handle_finish( READ_LOCK, $dbh, $sth, $Table{'users'});

  $results{'status_msg'} = 'Your user registration has been recorded successfully. Your login name is <strong>' . $inputs->{'dn'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.';
  return( 0, %results );
}

1;
