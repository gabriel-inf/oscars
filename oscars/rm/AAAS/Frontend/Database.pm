package AAAS::Frontend::Database;

# database.pm:  package for database operation
# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

use constant READ_LOCK => 1;
use constant WRITE_LOCK => 2;

our @ISA = qw(Exporter);
our @EXPORT = qw(db_handle_query db_handle_finish database_connect database_disconnect query_prepare query_execute query_finish database_lock_table database_unlock_table %table %table_field READ_LOCK WRITE_LOCK);

use DBI;


##### Settings Begin (Global variables) #####
# database connection info
%db_connect_info = (
  'database' => 'AAAS',
  'host' => 'localhost',
  'user' => 'davidr',
  'password' => 'shyysh'
);

# database table names
%table = (
  'users' => 'users',
);

# database field names
# usage: @{ $table_field{'users'} }{'dn', 'password', 'level'}
%table_field = (
  'users' => {
      'id' => 'user_id',
      'last_name' => 'user_last_name',
      'first_name' => 'user_first_name',
      'dn' => 'user_dn',
      'password' => 'user_password',
      'email_primary' => 'user_email_primary',
      'email_secondary' => 'user_email_secondary',
      'phone_primary' => 'user_phone_primary',
      'phone_secondary' => 'user_phone_secondary',
      'description' => 'user_description',
      'level' => 'user_level',
      'register_time' => 'user_register_time',
      'activation_key' => 'user_activation_key',
      'pending_level' => 'user_pending_level',
      'authorization_id' => 'authorization_id',
      'institution_id' => 'institution_id',
  }
);

##### Settings End #####


##### sub db_handle_query
# In:  database handle, partial query, table name, arglist
# Out: error code, number of rows returned, statement handle
# Out: $Err_Code (0 on success), $DB_Handle
sub db_handle_query
{
  my ($opflag, $dbh, $query, $table_name, @arglist) = @_;
  my $error_code = database_lock_table($opflag, $table{$table_name});
  if ( $error_code ) {
      database_disconnect( $dbh );
      return( $error_code, 0, undef );
  }
  $query =~ s/WHERE/FROM $table{$table_name} WHERE/;
  #print STDERR "** ", ' ', $query, "\n\n";
  ( $error_code, $sth ) = query_prepare( $dbh, $query );
  if ( $error_code ) {
      database_unlock_table($opflag, $table{$table_name});
      database_disconnect( $dbh );
      return( $error_code, 0, undef );
  }
  ( $error_code, $num_rows ) = query_execute( $sth, @arglist );

  if ( $error_code ) {
      print STDERR $error_code, " after exec\n\n";
      database_unlock_table($opflag, $table{$table_name});
      database_disconnect( $dbh );
      return( $error_code, 0, undef );
  }
  else { return ($error_code, $num_rows, $sth) };
}


##### sub db_handle_finish
# In:  database handle, statement handle, table name
# Out: None.
sub db_handle_finish
{
  my ($opflag, $dbh, $sth, $table_name) = @_;
  query_finish( $sth );
  database_unlock_table($opflag, $table{$table_name});
  database_disconnect( $dbh );
}



##### sub database_connect
# In: None
# Out: $Err_Code (0 on success), $DB_Handle
sub database_connect
{
  my $dsn = "DBI:mysql:database=$db_connect_info{'database'};host=$db_connect_info{'host'}";
  my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'} );

  if ( defined( $dbh ) ) {
      return (0, $dbh);
  }
  else {
      return (1, "CantConnectDB\n");
  }
}


##### sub database_disconnect
# In: $DB_Handle ($dbh)
# Out: None
sub database_disconnect
{
  my $dbh = $_[0];
  $dbh->disconnect();
}


##### sub query_prepare
# In: $DB_Handle ($dbh), $Statement
# Out: $Err_Code, $Statement_Handle
sub query_prepare
{
  my( $dbh, $Statement ) = @_;
  my $sth = $dbh->prepare( "$Statement" ) || return (1, "CantPrepareStatement\n" . $dbh->errstr);
	
    # if nothing fails, return 0 (success) and the statement handle
  return (0, $sth);
}


##### sub query_execute
# In: $Statement_Handle ($sth), @Query_Arguments (for placeholders(?s) in the prepared query statement)
# Out: $Err_Code, $Number_of_Rows_Affected
sub query_execute
{
  my( $sth, @Query_Args ) = @_;
    # execute the prepared query (run subroutine 'query_prepare' before
    # calling this subrutine)
  my $num_of_affected_rows = $sth->execute( @Query_Args ) || return (1, "CantExecuteQuery\n" . $sth->errstr);

    # if nothing fails, return 0 (success) and the $num_of_affected_rows
  return (0, $num_of_affected_rows);
}


##### sub query_finish
# In: $Statement_Handle ($sth)
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
    my ($opflag, $table_name) = @_;
    my $query = "LOCK TABLE $table{ $table_name }";
    if ($opflag & $WRITE_LOCK) { $query .= " WRITE"; }
    elsif ($opflag & READ_LOCK) { $query .= " READ"; }
    return (0, "");
}


##### sub database_unlock_table
# In:  opflag, table name
# Out: status, error message if any
sub database_unlock_table
{
    my ($opflag, $table_name) = @_;
    if (($opflag & READ_LOCK) || ($opflag & WRITE_LOCK))
    {
        $query = "UNLOCK TABLE $table{ $table_name }";
    }
    return (0, "");
}

# Don't touch the line below
1;
