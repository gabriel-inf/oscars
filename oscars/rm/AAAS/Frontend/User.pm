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
  my( $self, $inputs ) = @_;
  my( $query, $sth, %results );

  my( %table ) = $self->{'dbconn'}->get_AAAS_table('users');
    # get the password from the database
  $query = "SELECT $table{'users'}{'password'}, $table{'users'}{'level'} FROM users WHERE $table{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth ) = $self->{'dbconn'}->handle_query($query, 'users', $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return( 1, %results ); }

    # check whether this person is a registered user
  my $password_matches = 0;
  if ( $sth->rows == 0 ) {
        # this login name is not in the database
      $self->{'dbconn'}->handle_finish( 'users' );
      $results{'error_msg'} = 'Please check your login name and try again.';
      return( 1, %results );
  }
  else {
        # this login name is in the database; compare passwords
      while ( my $ref = $sth->fetchrow_arrayref ) {
          if ( $$ref[1] eq $non_activated_user_level ) {
                # this account is not authorized & activated yet
              $self->{'dbconn'}->handle_finish( 'users' );
              $results{'error_msg'} = 'This account is not authorized or activated yet.';
              return( 1, %results );
          }
          elsif ( $$ref[0] eq  $inputs->{'password'} ) {
              $password_matches = 1;
          }
      }
  }
  $self->{'dbconn'}->handle_finish( 'users' );

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
  my( $self, $inputs, $fields_to_display ) = @_;
  my( $sth, $query );
  my( %results );
  my( %table ) = $self->{'dbconn'}->get_AAAS_table('users');

    # DB query: get the user profile detail
  $query = "SELECT ";
  foreach $_ ( @$fields_to_display ) {
      $query .= $table{'users'}{$_} . ", ";
  }
    # delete the last ", "
  $query =~ s/,\s$//;
  $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth ) = $self->{'dbconn'}->handle_query($query, 'users', $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return( 1, %results ) };

  if ( $sth->rows == 0 )
  {
      $self->{'dbconn'}->handle_finish( 'users');
      $results{'error_msg'} = 'No such user in the database';
      return( 1, %results );
  }

    # populate %results with the data fetched from the database
  @results{@$fields_to_display} = ();
  $sth->bind_columns( map { \$results{$_} } @$fields_to_display );
  $sth->fetch();
  $self->{'dbconn'}->query_finish();

  $query = "SELECT institution_name FROM institutions WHERE institution_id = ?";
  ( $results{'error_msg'}, $sth ) = $self->{'dbconn'}->handle_query($query, 'institutions', $results{'institution_id'});
  if ( $results{'error_msg'} ) { return( 1, %results ) };

  if ( $sth->rows == 0 )
  {
      $self->{'dbconn'}->handle_finish( 'users' );
      $results{'error_msg'} = 'No such organization in the database';
      return( 1, %results );
  }
    # flatten it out
  while (my @data = $sth->fetchrow_array()) {
         $results{'institution'} = $data[0];
  }   


  $self->{'dbconn'}->handle_finish( 'users' );
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
  my ( $self, $inputs, @fields_to_read ) = @_;
  my( $sth, $query, $read_only_level, @read_only_user_levels );
  my( $update_password, $encrypted_password );   # TODO:  FIX
  my( %table ) = get_AAAS_table();
  my( %results );

    # user level provisioning:  # if the user's level equals one of the
    #  read-only levels, don't give them access 
  $query = "SELECT $table{'users'}{'level'} FROM users WHERE $table{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = $self->dbconn->handle_query($query, 'users', $inputs->{'dn'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

  while ( my $ref = $sth->fetchrow_arrayref ) {
      foreach $read_only_level ( @read_only_user_levels ) {
          if ( $$ref[0] eq $read_only_level ) {
              $self->{'dbconn'}->handle_finish( 'users' );
              $results{'error_msg'} = '[ERROR] Your user level (Lv. ' . $$ref[0] . ') has a read-only privilege and you cannot make changes to the database. Please contact the system administrator for any inquiries.';
              return ( 1, %results );
          }
      }
  }
  $self->{'dbconn'}->query_finish( $sth );

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

  ( $results{'error_msg'}, undef, $sth ) = $self->{'dbconn'}->handle_query($query, 'users', $inputs->{'dn'} );
  if ( $results{'error_msg'}) { return( 1, %results ); }

    # populate %results with the data fetched from the database
  @results{@fields_to_read} = ();
  $sth->bind_columns( map { \$results{$_} } @fields_to_read );
  $sth->fetch();

    ### check the current password with the one in the database before
    ### proceeding
  if ( $results{'password'} ne $inputs->{'password_current'} ) {
      $self->{'dbconn'}->handle_finish( 'users' );
      return( 1, 'Please check the current password and try again.' );
  }
  $self->{'dbconn'}->query_finish( $sth );

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
      if ( $results{$_} ne $inputs->{$_} ) {
          push( @fields_to_update, $table{'users'}{$_} );
          push( @values_to_update, $inputs->{$_} );
      }
  }

    # if there is nothing to update...
  if ( $#fields_to_update < 0 ) {
      $self->{'dbconn'}->unlock_table( 'users' );
      return( 1, 'There is no changed information to update.' );
  }

    # prepare the query for database update
  $query = "UPDATE users SET ";
  foreach $_ ( 0 .. $#fields_to_update ) {
      $query .= $fields_to_update[$_] . " = ?, ";
  }
  $query =~ s/,\s$//;
  $query .= " FROM users WHERE $table{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, undef, $sth ) = $self->{'dbconn'}->handle_query($query, 'users', @values_to_update, $inputs->{'dn'} );
  if ( $results{'error_msg'}) {
      $results{'error_msg'} =~ s/CantExecuteQuery\n//;
      $results{'error_msg'} =  'An error has occurred while updating your account information.<br>[Error] ' . $results{'error_msg'};
      return( 1, %results );
  }

  $self->{'dbconn'}->handle_finish( 'users' );
  $results{'status_msg'} = 'Your account information has been updated successfully.';
  return( 0, %results );
}


# activateaccount:  Account Activation DB methods

##### method activate_account
# In: reference to hash of parameters
# Out: status code, status message
sub activate_account
{
  my( $self, $inputs ) = @_;
  my( $sth, $query );
  my( %table ) = get_AAAS_table();
  my( %results );

    # get the password from the database
  $query = "SELECT $table{'users'}{'password'}, $table{'users'}{'activation_key'}, $table{'users'}{'pending_level'} FROM users WHERE $table{'users'}{'dn'} = ?";

  ( $results{'error_msg'}, $sth) = $self->dbconn->handle_query($query, 'users', $inputs->{'dn'});
  if ( $results{'error_msg'} ) { return ( 1, %results ); }

      # check whether this person is a registered user
  my $keys_match = 0;
  my( $pending_level, $non_match_error );

  if ( $sth->rows == 0 ) {
        # this login name is not in the database
      $self->{'dbconn'}->handle_finish( 'users' );
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
  $self->{'dbconn'}->( $sth );

      ### if the input password and the activation key matched against those
      ### in the database, activate the account
  if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
      $query = "UPDATE users SET $table{'users'}{'level'} = ?, $table{'users'}{'pending_level'} = ?, $table{'users'}{'activation_key'} = '' WHERE $table{'users'}{'dn'} = ?";

      ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'users', $pending_level, $inputs->{'dn'});
      if ( $results{'error_msg'} ) { return( 1, %results ); }
  }
  else {
      $self->{'dbconn'}->handle_finish( 'users' );
      results{'error_msg'} = $non_match_error;
      return( 1, %results );
  }
  $self->{'dbconn'}->handle_finish( 'users' );
  results{'status_msg'} = 'The user account <strong>' . $inputs->{'dn'} . '</strong> has been successfully activated. You will be redirected to the main service login page in 10 seconds.<br>Please change the password to your own once you sign in.';
  return( 0, %results );
}


# register:  user account registration db

##### method process_registration
# In:  reference to hash of parameters
# Out: status message
sub process_registration
{
  my( $self, $inputs, @insertions ) = @_;
  my( $sth, $query );
  my( %results );

  my( %table ) = get_AAAS_table();
  my $encrypted_password = $inputs->{'password_once'};

    # get current date/time string in GMT
  my $current_date_time = $inputs ->{'utc_seconds'};
	
    # login name overlap check
  $query = "SELECT $table{'users'}{'dn'} FROM users WHERE $table{'users'}{'dn'} = ?";
  if ( $results{'error_msg'} ) { return( 1, %results ); }

  if ( $sth->rows > 0 ) {
      $self->{'dbconn'}->handle_finish( 'users' );
      results{'error_msg'} = 'The selected login name is already taken by someone else; please choose a different login name.';
      return( 1, %results );
  }

  $self->{'dbconn'}->query_finish( $sth );

  $query = "INSERT INTO users VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

  ( $results{'error_msg'}, $sth) = $self->{'dbconn'}->handle_query($query, 'users', @insertions);
  if ( $results{'error_msg'} ) {
      $results{'error_msg'} =~ s/CantExecuteQuery\n//;
      $results{'error_msg'} = 'An error has occurred while recording your registration on the database. Please contact the webmaster for any inquiries.<br>[Error] ' . $results{'error_msg'};
      return( 1, %results );
  }
  $self->{'dbconn'}->handle_finish( 'users' );

  $results{'status_msg'} = 'Your user registration has been recorded successfully. Your login name is <strong>' . $inputs->{'dn'} . '</strong>. Once your registration is accepted, information on activating your account will be sent to your primary email address.';
  return( 0, %results );
}

1;
