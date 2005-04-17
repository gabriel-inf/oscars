package DB;

# DB.pm:  package for database operation
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use strict;

require Exporter;

use constant READ_LOCK => 1;
use constant WRITE_LOCK => 2;

our @ISA = qw(Exporter);
our @EXPORT = qw(db_handle_query db_handle_finish database_connect database_disconnect query_prepare query_execute query_finish database_lock_table database_unlock_table READ_LOCK WRITE_LOCK);

use DBI;

our %db_connect_info = (
  'host' => 'localhost',
  'user' => 'davidr',
  'password' => 'shyysh'
);


##### sub db_handle_query
# In:  database handle, partial query, table name, arglist
# Out: error code, number of rows returned, statement handle
# Out: $error_msg (0 on success), $DB_handle
sub db_handle_query
{
  my ($dbh, $query, $table, $opflag, @arglist) = @_;
  my ($sth, $num_rows); 

  if (!defined($dbh)) { print STDERR "foo\n"; }
  #print STDERR $query, "\n";
  my $error_msg = database_lock_table($opflag, $table);
  if ( $error_msg ) {
      database_disconnect( $dbh );
      return( $error_msg, undef );
  }
  ( $error_msg, $sth ) = query_prepare( $dbh, $query );
  if ( $error_msg ) {
      database_unlock_table($opflag, $table);
      database_disconnect( $dbh );
      return( $error_msg, undef );
  }
  ($error_msg, $num_rows) = query_execute( $sth, @arglist );

  if ( $error_msg ) {
      print STDERR $error_msg, " after exec\n\n";
      database_unlock_table($opflag, $table);
      database_disconnect( $dbh );
      return( $error_msg, undef );
  }
  else { return ('', $num_rows, $sth) };
}


##### sub db_handle_finish
# In:  database handle, statement handle, table name
# Out: None.
sub db_handle_finish
{
  my ($opflag, $dbh, $sth, $table) = @_;
  query_finish( $sth );
  database_unlock_table($opflag, $table);
  database_disconnect( $dbh );
}



##### sub database_connect
# In: None
# Out: $error_msg (empty on success), $DB_handle
sub database_connect
{
  my($dbname) = @_;
  my $dsn = "DBI:mysql:database=$dbname;
  host=$db_connect_info{'host'}";
  my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'} );

  if ( defined( $dbh ) ) { return ( '', $dbh ); }
  else { return ( "CantConnectDB\n", undef ); }
}


##### sub database_disconnect
# In: $DB_handle ($dbh)
# Out: None
sub database_disconnect
{
  my $dbh = $_[0];
  $dbh->disconnect();
}


##### sub query_prepare
# In: $DB_handle ($dbh), $statement
# Out: $error_msg, $statement_handle
sub query_prepare
{
  my( $dbh, $statement ) = @_;
  my $sth = $dbh->prepare( $statement );
  if (!defined($sth)) { return ("CantPrepareStatement\n" . $dbh->errstr, undef); }
    # if nothing fails, return an empty error message, and the statement
    # handle
  return ('', $sth);
}


##### sub query_execute
# In: $statement_handle ($sth), @query_Arguments (for placeholders(?s) in the prepared query statement)
# Out: $error_msg, $num_of_rows_affected
sub query_execute
{
  my( $sth, @query_args ) = @_;
    # execute the prepared query (run subroutine 'query_prepare' before
    # calling this subrutine)
  my $num_rows = $sth->execute( @query_args );
  if (!$num_rows) {
      return( "CantExecuteQuery\n" . $sth->errstr, undef );
  }
    # if nothing fails, empty error message, and $num_rows is set
  return ( '', $num_rows);
}


##### sub query_finish
# In: $statement_handle ($sth)
# Out: None
sub query_finish
{
  my ($sth) = @_;
  $sth->finish();
}


##### sub database_lock_table
# In:  opflag, table name 
# Out: status, error message if any
sub database_lock_table
{
    my ($opflag, $table) = @_;
    my $query = "LOCK TABLE $table";
    if ($opflag & WRITE_LOCK) { $query .= " WRITE"; }
    elsif ($opflag & READ_LOCK) { $query .= " READ"; }
    return ( '' );
}


##### sub database_unlock_table
# In:  opflag, table name
# Out: status, error message if any
sub database_unlock_table
{
    my ($opflag, $table) = @_;
    my $query;

    if (($opflag & READ_LOCK) || ($opflag & WRITE_LOCK))
    {
        $query = "UNLOCK TABLE $table";
    }
    return ( '' );
}

# Don't touch the line below
1;
