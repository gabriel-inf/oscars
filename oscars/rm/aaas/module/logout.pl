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
my( $Dbh, $Sth, $Error_Status, $Query );

# connect to the database
( $Dbh, $Error_Status ) = &Database_Connect();
if ( $Error_Status != 1 )
{
	return( $Error_Status );
}

# TODO:  lock table
# TODO:  redo to change status of user in user table

&Query_Finish( $Sth );

# TODO:  unlock the table(s)

# disconnect from the database
&Database_Disconnect( $Dbh );
}

##### End of sub routines #####

##### End of script #####
