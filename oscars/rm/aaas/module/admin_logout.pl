#!/usr/bin/perl

# logout.pl:  Admin tool: DB operations associated with logout
# Last modified: April 7, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require 'general.pl';
require 'database.pl';

sub Handle_Admin_Logout(FormData)
{
    # connect to the database
    ( $Dbh, $Error_Status ) = &Database_Connect();
    if ( $Error_Status != 1 )
    {
        return( $Error_Status );
    }

    # TODO:  lock table

    &Query_Finish( $Sth );

    # TODO:  unlock the operation

    # disconnect from the database
    &Database_Disconnect( $Dbh );

    # TODO:  return status
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### End of sub routines #####

##### End of script #####
