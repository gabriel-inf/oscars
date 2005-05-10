#!/usr/bin/perl

# editprofile.pl:  Admin tool: Edit Admin Profile page
# Last modified: April 24, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;

require '../lib/general.pl';


my (%form_params, %results);

my $cgi = CGI->new();
my $error_status = check_login(undef, $cgi);

if (!$error_status)
{
  foreach $_ ($cgi->param) {
      $form_params{$_} = $cgi->param($_);
  }
  $results{'error_msg'} = validate_params(\%form_params);
  if (!$results{'error_msg'})
  {
      my( $encrypted_passwd, $update_password );

          # encrypt the new password
      $encrypted_passwd = encode_passwd( $form_params->{'password_new_once'} );
      $update_password = 1;

      ($error_status, %results) = soap_admin_set_profile(\%form_params);
      if (!$error_status) {
          update_frames("main_frame", "", $results{'status_msg'});
          print_profile_update(\%results);
      }
      else {
          update_frames("main_frame", "", $results{'error_msg'});
      }
  }
  else {
      update_frames("main_frame", "", $results{'error_msg'});
  }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}


exit;



##### sub validate_params
# In: None
# Out: None
sub validate_params
{
  my( $form_params ) = @_;

      # validate user input ... Javascript also takes care of form data validation)
  if ( $FormData{'password_current'} eq '' )
  {
      return ( 'Please enter the current password.' );
  }
  if ( $form_params->{'password_new_once'} ne '' || $form_params->{'password_new_twice'} ne '' )
  {
      if ( $form_params->{'password_new_once'} ne $form_params->{'password_new_twice'} )
      {
          return ( 'Please enter the same new password twice for verification.' );
      }
  }
  return( '' );
}


##### sub print_profile_update
# In: None
# Out: None
sub print_profile_update
{
  my( $form_params ) = @_;

      ### TODO:  connect to AAAS, and get back result
      ### when everything has been processed successfully...

#	'The account information has been updated successfully.' );
}
