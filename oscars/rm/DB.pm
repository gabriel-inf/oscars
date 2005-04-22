package OSCARS_db;

# OSCARS_db.pm:  convenience class for database operations, inherited by
#                front end database handling classes
# Last modified: April 21, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use strict;

use DBI;

use constant READ_LOCK => 1;
use constant WRITE_LOCK => 2;



######################################################################
sub new {
  my ( $_class, %_args ) = @_;
  my ( $_self ) = {%_args};
  
  # Bless $self into designated class.
  bless($_self, $_class);
  
  # Initialize.
  $_self->initialize();
  
  return($_self);
}

######################################################################
sub initialize {
    my ( $_self ) = @_;
    $_self->{'dbh'} = DBI->connect($_self->{'configs'}->{'db_use_database'}, 
             $_self->{'configs'}->{db_login_name}, $_self->{'configs'}->{'db_login_passwd'})
            or die "Couldn't connect to database: " . DBI->errstr;
    $_self->{'sth'} = undef;
    $_self->{'lock_type'} = READ_LOCK;
}


#####
sub set_lock_type
{
  my( $self ) = @_;
  $self->{'lock_type'} = @_;
}


##### method handle_query
# In:  query statement, table name, arglist
# Out: $error_msg (empty on success)
sub handle_query
{
  my ($self, $query, $table_name, @arglist) = @_;

  if (!defined($self->{'dbh'}))
  {
     print STDERR "foo\n";
  }
  my $error_msg = $self->lock_table($table_name);
  if ( $error_msg ) { return( $error_msg, undef ); }

  #print STDERR $query, "\n";
  ( $error_msg, $self->{'sth'} ) = $self->query_prepare( $query );
  if ( $error_msg ) {
      $self->unlock_table($table_name);
      return( $error_msg, undef );
  }
  $error_msg = $self->query_execute( @arglist );

  if ( $error_msg ) {
      print STDERR $error_msg, " after exec\n\n";
      $self->unlock_table($table_name);
      return( $error_msg, undef );
  }
  else { return ('', $self->{'sth'} ) };
}


##### method handle_finish
# In:  lock type flag, table name
# Out: None.
sub handle_finish
{
  my ($self, $table) = @_;
  $self->query_finish();
  $self->unlock_table($table);
}



##### method query_prepare
# In:  query statement 
# Out: $error_msg
sub query_prepare
{
  my( $self, $statement ) = @_;
  my $sth = $self->{'dbh'}->prepare( $statement );
  if (!defined($sth)) { return ("CantPrepareStatement\n" . $self->{'dbh'}->errstr, undef); }
    # if nothing fails, return an empty error message
  return ('', $sth);
}


##### method query_execute
# In: @query_args (placeholders for '?''s in the prepared query statement)
# Out: $error_msg (empty on success)
sub query_execute
{
  my( $self, @query_args ) = @_;
    # execute the prepared query (run subroutine 'query_prepare' before
    # calling this subrutine)
  #print '-- ', @query_args, "\n";
  my $num_rows = $self->{'sth'}->execute( @query_args );
  if (!$num_rows) {
      return( "CantExecuteQuery\n" . $self->{'sth'}->errstr );
  }
    # if nothing fails, empty error message
  return ( '' );
}


##### sub query_finish
# In:  None 
# Out: None
sub query_finish
{
  my ($self) = @_;
  $self->{'sth'}->finish();
  $self->{'sth'} = undef;
}


##### method lock_table
# In:  table name 
# Out: error message if any
sub lock_table
{
    my ($self, $table_name) = @_;
    my $query = "LOCK TABLE $table_name";
    if ($self->{'lock_type'} & WRITE_LOCK) { $query .= " WRITE"; }
    elsif ($self->{'lock_type'} & READ_LOCK) { $query .= " READ"; }
    return ( '' );
}


##### method unlock_table
# In:  table name
# Out: error message if any
sub unlock_table
{
    my ($self, $table_name) = @_;
    my $query;

    if (($self->{'lock_type'} & READ_LOCK) || ($self->{'lock_type'} & WRITE_LOCK))
    {
        $query = "UNLOCK TABLE $table_name";
    }
    return ( '' );
}

# Don't touch the line below
1;
