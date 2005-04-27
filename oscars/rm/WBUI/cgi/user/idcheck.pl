#!/usr/bin/perl -w

# idcheck.pl:  User login name overlap check
# Last modified: April 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)


use AAAS::Client::SOAPClient;

require '../lib/general.pl';


# smiley icons used in the login name overlap check result page
%icon_locations = (
  'smile' => '../images/icon_biggrin.gif',	# smile face
  'sad' => '../images/icon_sad.gif',		# sad face
  'exclaim' => '../images/icon_exclaim.gif'	# "!" mark
);


my ($error_status, $cgi, %results);

($error_status, $cgi) = check_login(0);

if (!$error_status) {
  ($error_status, %results) = print_result_screen(\$cgi->param));
  if (!$error_status) {
      update_status_frame("");
  }
}

exit;


##### sub print_result_screen
# In:  ref to form parameter hash
# Out: None (exits the program at the end)
sub print_result_screen
{
  my( $form_params ) = @_;
  my(%results);
  my $results{'error_msg'} = '<strong>' . $form_params->{'id'} . '</strong><br>';

  if ( $form_params->{'id'} =~ /\W|\s/) {
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

  $results{'status_msg'} = 'OK';
  return( 0, %results );
}
