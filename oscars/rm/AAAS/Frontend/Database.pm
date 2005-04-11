package AAAS::Frontend::Database;

# database.pm:  package for database operation
# Last modified: April 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(database_connect database_disconnect query_prepare query_execute query_finish database_lock_table database_unlock_table %table %table_field);

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
# usage: @{ $table_field{'users'} }{'user_loginname', 'user_password', 'user_level'}
%table_field = (
  'users' => {
      'user_id' => 'user_id',
      'user_last_name' => 'user_last_name',
      'user_first_name' => 'user_first_name',
      'user_dn' => 'user_dn',
      'user_password' => 'user_password',
      'user_email_primary' => 'user_email_primary',
      'user_email_secondary' => 'user_email_secondary',
      'user_phone_primary' => 'user_phone_primary',
      'user_phone_secondary' => 'user_phone_secondary',
      'user_description' => 'user_description',
      'user_level' => 'user_level',
      'user_register_time' => 'user_register_time',
      'user_activation_key' => 'user_activation_key',
      'user_pending_level' => 'user_pending_level',
      'authorization_id' => 'authorization_id',
      'institution_id' => 'institution_id',
  }
);

##### Settings End #####


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
  my $sth = $_[0];
  $sth->finish();
}


##### sub database_lock_table
# In: 
# Out: None
sub database_lock_table
{
    return 0;
}


##### sub database_unlock_table
# In: 
# Out: None
sub database_unlock_table
{
    return 0;
}

# Don't touch the line below
1;
