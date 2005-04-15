#!/usr/bin/perl -w

# idcheck.pl:  User login name overlap check
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require 'soapclient.pl';

# smiley icons used in the login name overlap check result page
%icon_locations = (
  'smile' => '../images/icon_biggrin.gif',	# smile face
  'sad' => '../images/icon_sad.gif',		# sad face
  'exclaim' => '../images/icon_exclaim.gif'	# "!" mark
);


# Receive data from HTML form (accept all methods (POST/GET))
%FormData = &Parse_Form_Input_Data( 'all' );

my ($Error_Status, %Results) = &print_result_screen();
&Update_Frames();

exit;



##### Beginning of sub routines #####


##### sub print_result_screen
# In: None
# Out: None (exits the program at the end)
sub print_result_screen
{
  my(%results);
  my $results{'error_msg'} = '<strong>' . $FormData{'id'} . '</strong><br>';

  if ( $FormData{'id'} =~ /\W|\s/) {
      $results{'error_msg'} .= '<img src="' . $icon_locations{'exclaim'} . '" alt="!"> Please use only alphanumeric characters or _ for login name.';
      return( 1, %results );
  }

  my $overlap_check_result = &Check_Loginname_Overlap();
  if ( $overlap_check_result eq 'no' ) {
      $results{'error_msg'} .= '<img src="' . $icon_locations{'smile'} . '" alt="smile face"> You can use this login ID.';
      return( 1, %results ); 
  }

  if ( $overlap_check_result eq 'yes' )
  {
      $results{'error_msg'} .= '<img src="' . $icon_locations{'sad'} . '" alt="sad face"> This login name is already taken; please choose something else.';
      return( 1, %results );
  }


  # TODO:  print processing result to browser screen
  print "Pragma: no-cache\n";
  print "Cache-control: no-cache\n";
  print "Content-type: text/html\n\n";

  $results{'status_msg'} = 'OK';
  return( 0, %results );
}


##### End of sub routines #####

##### End of script #####
