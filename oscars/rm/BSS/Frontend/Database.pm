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
	'reservation_start_time' => 'reservation_start_time',
	'reservation_end_time' => 'reservation_end_time',
        'reservation_qos' => 'reservation_qos',
        'reservation_status' => 'reservation_status',
        'reservation_description' => 'reservation_description',
        'reservation_created_time' => 'reservation_created_time',
        'reservation_ingress_port' => 'reservation_ingress_port',
        'reservation_egress_port' => 'reservation_egress_port',
        'ingress_interface_id' => 'ingress_interface_id',
        'egress_interface_id' => 'egress_interface_id',
        'user_dn' => 'user_dn',
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
# Out: $Err_Code (0 on success), $DB_Handle
sub Database_Connect
{

	my $dsn = "DBI:mysql:database=$db_connect_info{'database'};host=$db_connect_info{'host'}";
#	my %attributes = ( 'RaiseError' => 0, 'PrintError' => 0 );
#	my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'}, \%attributes );
	my $dbh = DBI->connect( $dsn, $db_connect_info{'user'}, $db_connect_info{'password'} );

	if ( defined( $dbh ) )
	{
		return (0, $dbh);
	}
	else
	{
		return (1, "CantConnectDB\n");
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
# Out: $Err_Code, $Statement_Handle
sub Query_Prepare
{

	my( $dbh, $Statement ) = @_;

	my $sth = $dbh->prepare( "$Statement" ) || return (1, "CantPrepareStatement\n" . $dbh->errstr);
	
	# if nothing fails, return 0 (success) and the statement handle
	return (0, $sth);

}
##### End of sub Query_Prepare


##### sub Query_Execute
# In: $Statement_Handle ($sth), @Query_Arguments (for placeholders(?s) in the prepared query statement)
# Out: $Err_Code, $Number_of_Rows_Affected
sub Query_Execute
{

	my( $sth, @Query_Args ) = @_;

	# execute the prepared query (run subroutine 'Query_Prepare' before calling this subrutine)
	my $num_of_affected_rows = $sth->execute( @Query_Args ) || return (1, "CantExecuteQuery\n" . $sth->errstr);
	
	# if nothing fails, return 0 (success) and the $num_of_affected_rows
	return (0, $num_of_affected_rows);

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

##### sub Database_Lock_Table
# In: 
# Out: None
sub Database_Lock_Table
{
    return 0;
}
##### sub End of Database_Lock_Table

##### sub Database_Unlock_Table
# In: 
# Out: None
sub Database_Unlock_Table
{
    return 0;
}
##### sub End of Database_Unlock_Table


##### End of Library File
# Don't touch the line below
1;
