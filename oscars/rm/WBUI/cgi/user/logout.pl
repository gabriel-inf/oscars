#!/usr/bin/perl

# logout.pl:  Main Service: Logout Link
# Last modified: April 5, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};


##### Beginning of mainstream #####

# forward the user to the login screen
&Print_Frames('https://oscars.es.net/');

exit;

##### Beginning of sub routines #####


##### End of sub routines #####

##### End of script #####
