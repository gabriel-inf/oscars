#!/usr/bin/perl

# idcheck.pl:  User login name overlap check
# Last modified: April 1, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

# current script name (used for error message)
$script_filename = $ENV{'SCRIPT_NAME'};

# smiley icons used in the login name overlap check result page
%icon_locations = (
	'smile' => '../images/icon_biggrin.gif',	# smile face
	'sad' => '../images/icon_sad.gif',		# sad face
	'exclaim' => '../images/icon_exclaim.gif'	# "!" mark
);

##### Beginning of mainstream #####

# Receive data from HTML form (accept all methods (POST/GET))
# this hash is the only global variable used throughout the script
%FormData = &Parse_Form_Input_Data( 'all' );

if ( $FormData{'mode'} eq 'idcheck' )
{
	&Print_Result_Screen();
}

exit;

##### End of mainstream #####


##### Beginning of sub routines #####


##### sub Print_Result_Screen
# In: None
# Out: None (exits the program at the end)
sub Print_Result_Screen
{

	my $Processing_Result_Message = '<strong>' . $FormData{'id'} . '</strong><br>';

	if ( $FormData{'id'} =~ /\W|\s/)
	{
		$Processing_Result_Message .= '<img src="' . $icon_locations{'exclaim'} . '" alt="!"> Please use only alphanumeric characters or _ for login name.';
	}
	else
	{
		my $Overlap_Check_Result = &Check_Loginname_Overlap();

		if ( $Overlap_Check_Result eq 'no' )
		{
			$Processing_Result_Message .= '<img src="' . $icon_locations{'smile'} . '" alt="smile face"> You can use this login ID.';
		}
		elsif ( $Overlap_Check_Result eq 'yes' )
		{
			$Processing_Result_Message .= '<img src="' . $icon_locations{'sad'} . '" alt="sad face"> This login name is already taken; please choose something else.';
		}
	}


	# TODO:  print processing result to browser screen
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	exit;

}
##### End of sub Print_Result_Screen


##### End of sub routines #####

##### End of script #####
