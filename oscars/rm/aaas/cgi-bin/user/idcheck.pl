#!/usr/bin/env perl

# idcheck.pl:  User login name overlap check
# Last modified: March 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require '../lib/database.pl';

# template html file name for printing browser screen
$interface_template_filename = 'idcheck.html';

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

	# open html template file
	open( F_HANDLE, $interface_template_filename ) || &Print_Error_Screen( $script_filename, "FileOpen\n" . $interface_template_filename . ' - ' . $! );
	my @Template_Html = <F_HANDLE>;
	close( F_HANDLE );

	# print processing result to browser screen
	print "Pragma: no-cache\n";
	print "Cache-control: no-cache\n";
	print "Content-type: text/html\n\n";

	foreach $Html_Line ( @Template_Html )
	{
		foreach ( $Html_Line )
		{
			s/<!-- \(\(_Processing_Result_Message_\)\) -->/$Processing_Result_Message/g;
		}

		print $Html_Line;
	}

	exit;

}
##### End of sub Print_Result_Screen


##### sub Check_Loginname_Overlap
# In: None
# Out: $Check_Result [yes(overlaps)/no(doesn't overlap)]
sub Check_Loginname_Overlap
{
	### start working with the database
	my( $Dbh, $Sth, $Error_Status, $Query, $Num_of_Affected_Rows );

	( $Dbh, $Error_Status ) = &Database_Connect();
	if ( $Error_Status != 1 )
	{
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	# check whether a particular user id already exists in the database

	# database table & field names to check
	my $Table_Name_to_Check = $db_table_name{'users'};
	my $Field_Name_to_Check = $db_table_field_name{'users'}{'user_loginname'};

	# Query: select user_loginname from users where user_loginname='some_id_to_check';
	$Query = "SELECT $Field_Name_to_Check FROM $Table_Name_to_Check WHERE $Field_Name_to_Check = ?";

	( $Sth, $Error_Status ) = &Query_Prepare( $Dbh, $Query );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	( $Num_of_Affected_Rows, $Error_Status ) = &Query_Execute( $Sth, $FormData{'id'} );
	if ( $Error_Status != 1 )
	{
		&Database_Disconnect( $Dbh );
		&Print_Error_Screen( $script_filename, $Error_Status );
	}

	my $Check_Result;

	if ( $Num_of_Affected_Rows == 0 )
	{
		# ID does not overlap; usable
		$Check_Result = 'no';
	}
	else
	{
		# ID is already taken by someone else; unusable
		$Check_Result = 'yes';
	}

	&Query_Finish( $Sth );

	# disconnect from the database
	&Database_Disconnect( $Dbh );

	return $Check_Result;

}
##### End of sub Check_Loginname_Overlap


##### End of sub routines #####

##### End of script #####
