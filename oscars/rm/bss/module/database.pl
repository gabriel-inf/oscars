#

# database.pl
#
# library for database operation
# Last modified: April 7, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use DBI;

##### Settings Begin (Global variables) #####
# database connection info
%db_connect_info = (
	'database' => 'aaas',
	'host' => 'localhost',
	'user' => 'davidr',
	'password' => 'shyysh'
);

# database table names
%db_table_name = (
	'reservations' => 'reservations',
);

# reservations/past_reservations field names
%reservations_tables_field_names = (
	'reservation_id' => 'reservation_id',
	'user_loginname' => 'user_loginname',
	'reserv_origin_ip' => 'reserv_origin_ip',
	'reserv_dest_ip' => 'reserv_dest_ip',
	'reserv_bandwidth' => 'reserv_bandwidth',
	'reserv_start_time' => 'reserv_start_time',
	'reserv_end_time' => 'reserv_end_time',
	'reserv_description' => 'reserv_description',
	'reserv_made_time' => 'reserv_made_time',
	'access_ip' => 'access_ip',
	'access_domain' => 'access_domain',
	'access_browser' => 'access_browser'
);

# database field names
%db_table_field_name = (
	'reservations' => { %reservations_tables_field_names }
);

##### Settings End #####


##### List of sub routines #####
# sub Database_Connect
# sub Database_Disconnect
# sub Query_Prepare
# sub Query_Execute
# sub Query_Finish
##### List of sub routines End #####


##### sub Database_Connect
# In: None
# Out: $DB_Handle [$dbh/'' (fail)], $Error_Status [1 (success)/"$Err_Code\n" (fail)]
sub Database_Connect
{

	my $dsn = "DBI:mysql:database=$db_connect_info{'database'};host=$db_connect_info{'host'}";
#	my %attributes = ( 'RaiseError' => 0, 'PrintError' => 0 );
#	my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'}, \%attributes );
	my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'} );

	if ( defined( $dbh ) )
	{
		return $dbh, 1;
	}
	else
	{
		return '', "CantConnectDB\n";
	}

}
##### End of sub Database_Connect


##### sub Database_Disconnect
# In: $DB_Handle ($dbh)
# Out: None
sub Database_Disconnect
{

	my $dbh = $_[0];

	$dbh->disconnect();

}
##### End of sub Database_Disconnect


##### sub Query_Prepare
# In: $DB_Handle ($dbh), $Statement
# Out: $Statement_Handle [$sth/'' (fail)], $Error_Status [1 (success)/"$Err_Code\n$Errno" (fail)]
# (1: success, $Err_Code: reason of failure, $Errno: $dbh->errstr)
sub Query_Prepare
{

	my( $dbh, $Statement ) = @_;

	my $sth = $dbh->prepare( "$Statement" ) || return '', "CantPrepareStatement\n" . $dbh->errstr;
	
	# if nothing fails, return the statement handle and 1 (success) for error status
	return $sth, 1;

}
##### End of sub Query_Prepare


##### sub Query_Execute
# In: $Statement_Handle ($sth), @Query_Arguments (for placeholders(?s) in the prepared query statement)
# Out: $Number_of_Rows_Affected [$num_of_affected_rows /'' (fail)], $Error_Status [1 (success)/"$Err_Code\n$Errno" (fail)]
# (1: success, $Err_Code: reason of failure, $Errno: $sth->errstr)
sub Query_Execute
{

	my( $sth, @Query_Args ) = @_;

	# execute the prepared query (run subroutine 'Query_Prepare' before calling this subrutine)
	my $num_of_affected_rows = $sth->execute( @Query_Args ) || return '', "CantExecuteQuery\n" . $sth->errstr;
	
	# if nothing fails, return the $num_of_affected_rows and 1 (success) for error status
	return $num_of_affected_rows, 1;

}
##### End of sub Query_Execute


##### sub Query_Finish
# In: $Statement_Handle ($sth)
# Out: None
sub Query_Finish
{

	my $sth = $_[0];

	$sth->finish();

}
##### sub End of Query_Finish


##### End of Library File
# Don't touch the line below
1;
