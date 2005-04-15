#!/usr/bin/perl -w

# myprofile.pl:  Main service: My Profile page
# Last modified: April 15, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require 'soapclient.pl';



# Receive data from HTML form (accept POST method only)
%FormData = &Parse_Form_Input_Data( 'post' );

my ($Error_Status, %Results = &process_profile_update();
if ( !$Error_Status)
{
    ### TODO:  output screen
    &Update_Frames('', $Results{'status_msg'});
}
else { &Update_Frames('', $Results{'error_msg'}); }

exit;



##### Beginning of sub routines #####


##### sub process_profile_update
# In: None
# Out: status message
sub process_profile_update
{

  my(%results);

    # validate user input (fairly minimal... Javascript also takes care of form data validation)
  if ( $FormData{'password_current'} eq '' )
  {
      $results{'error_msg'} = 'Please enter the current password.';
      return( 1, %results );
  }

  my $encrypted_password;
  my $update_password = 0;
  my(%params);

  if ( $FormData{'password_new_once'} ne '' || $FormData{'password_new_twice'} ne '' )
  {
      if ( $FormData{'password_new_once'} ne $FormData{'password_new_twice'} )
      {
          $results{'error_msg'} = 'Please enter the same new password twice for verification.';
          return( 1, %results );
      }
      else
      {
           # encrypt the new password
          $params{'password'} = &Encode_Passwd( $FormData{'password_new_once'} );

          $update_password = 1;
      }
  }

    ### TODO:  make call to database, get status

    ### when everything has been processed successfully...
  results{'status_msg'} = 'Your account information has been updated successfully.';
  return( 0, %results );
}

##### End of sub process_profile_update

##### End of sub routines #####

##### End of script #####
