#!/usr/bin/perl

# logout.pl:  DB operations associated with logout
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

##### Beginning of sub routines #####

sub Handle_Logout()
{
### delete this cookie data from the cookiekey table
my( $Dbh, $Sth, $Error_Status, $Query );

# connect to the database
( $Dbh, $Error_Status ) = &Database_Connect();
if ( $Error_Status != 1 )
{
	return( $Error_Status );
}

# TODO:  lock table
# TODO:  redo to change status of user in user table
# delete any previously set random keys for the same login name
$Query = "DELETE FROM $db_table_name{'cookiekey'} WHERE $db_table_field_name{'cookiekey'}{'cookiekey_id'} = ?";

( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
if ( $Error_Status != 1 )
{
	&Database_Disconnect( $Dbh );
	return( $Error_Status );
}

( undef, $Error_Status ) = &Query_Execute( $Sth, $Data_From_Cookie{'cookiekey_id'} );
if ( $Error_Status != 1 )
{
	&Database_Disconnect( $Dbh );
	return( $Error_Status );
}

&Query_Finish( $Sth );

# TODO:  unlock the table(s)

# disconnect from the database
&Database_Disconnect( $Dbh );
}

##### End of sub routines #####

##### End of script #####
