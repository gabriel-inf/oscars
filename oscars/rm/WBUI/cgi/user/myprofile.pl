#!/usr/bin/perl -w

# myprofile.pl:  Main service: My Profile page
# Last modified: April 25, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';

use AAAS::Client::SOAPClient;
use AAAS::Client::Auth;


    # names of the fields to be displayed on the screen
my @Fields_to_Display = ( 'last_name', 'first_name', 'dn', 'email_primary', 'email_secondary', 'phone_primary', 'phone_secondary', 'description', 'level', 'register_time', 'activation_key', 'pending_level', 'authorization_id', 'institution_id' );

my( %FormData );  # TODO:  edit_profile

# login URI
$login_URI = 'https://oscars.es.net/';
$auth = AAAS::Client::Auth->new();

if (!($auth->verify_login_status(\%FormData, undef))) 
{
    print "Location: $login_URI\n\n";
    exit;
}

( $Error_Status, %Results ) = soap_get_profile(\%FormData, \@Fields_to_Display);

if (!$Error_Status)
{
    print_profile(\%Results);
}
else
{
    Update_Frames("", $Results{'error_msg'});
}
exit;


##### Beginning of sub routines #####


sub print_profile
{
  my ($params) = @_;

  #print "Content-type: text/html\n\n";
  print "<html>\n";
  print "<head>\n";
  print "<link rel=\"stylesheet\" type=\"text/css\" ";
  print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
  print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/user/myprofile.js\"></script>\n";
  print "</head>\n\n";

  print "<body>\n\n";

  print "<script language=\"javascript\">print_navigation_bar(\"myprofile\");</script>\n\n";

  print "<div id=\"account_ui\">\n\n";

  print "<p><em>View/Edit My Profile</em><br>\n";
  print "(Required fields are marked with a <span class=\"requiredmark\">*</span>)</p>\n";
  print "<form method=\"post\" action=\"https://oscars.es.net/cgi-bin/user/myprofile.pl\" onsubmit=\"return check_form(this);\">\n";
 
  print "<table>\n";
  print "<tr>\n";

  print "  <th><span class=\"requiredmark\">*</span> Distinguished Name</th>\n";
  print "  <td><input type=\"text\" name=\"dn\" size=\"20\"\n";
  if (defined($params->{'dn'})) { print " value=\"$params->{'dn'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "   <th><span class=\"requiredmark\">*</span> Current Password</th>\n";
  print "   <td><input type=\"password\" name=\"password_current\" size=\"20\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "   <th>New Password (Enter twice; Leave blank to stay the same)</th>\n";
  print "   <td>\n";
  print "       <input type=\"password\" name=\"password_new_once\" size=\"20\" style=\"margin-bottom: .3em\"><br>\n";
  print "       <input type=\"password\" name=\"password_new_twice\" size=\"20\">\n";
  print "   </td>\n";
  print "</tr>\n";
  print "</table>\n";
  print "<table>\n";

  print "<tr>\n";
  print "  <th><span class=\"requiredmark\">*</span> First Name</th>\n";
  print "  <td><input type=\"text\" name=\"first_name\" size=\"20\"";
  if (defined($params->{'first_name'})) { print " value=\"$params->{'first_name'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th><span class=\"requiredmark\">*</span> Last Name</th>\n";
  print "  <td><input type=\"text\" name=\"last_name\" size=\"20\"";
  if (defined($params->{'last_name'})) { print " value=\"$params->{'last_name'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th><span class=\"requiredmark\">*</span> Organization</th>\n";
  print "  <td><input type=\"text\" name=\"institution\" size=\"40\"";
  if (defined($params->{'institution'})) { print " value=\"$params->{'institution'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th valign=\"top\">Personal Description</th>\n";
  print "  <td><textarea name=\"description\" rows=\"3\" cols=\"34\"";
  if (defined($params->{'description'})) { print " value=\"$params->{'description'}\""; }
  print "  </textarea></td>\n";
  print "</tr>\n";

  print "</table>\n\n";
  
  print "<table>\n";

  print "<tr>\n";
  print "  <th><span class=\"requiredmark\">*</span> E-mail (Primary)</th>\n";
  print "  <td><input type=\"text\" name=\"email_primary\" size=\"40\"";
  if (defined($params->{'email_primary'})) { print " value=\"$params->{'email_primary'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th>E-mail (Secondary)</th>\n";
  print "  <td><input type=\"text\" name=\"email_secondary\" size=\"40\"";
  if (defined($params->{'email_secondary'})) { print " value=\"$params->{'email_secondary'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th><span class=\"requiredmark\">*</span> Phone Number (Primary)</th>\n";
  print "  <td><input type=\"text\" name=\"phone_primary\" size=\"40\"";
  if (defined($params->{'phone_primary'})) { print " value=\"$params->{'phone_primary'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "<tr>\n";
  print "  <th>Phone Number (Secondary)</th>\n";
  print "  <td><input type=\"text\" name=\"phone_secondary\" size=\"40\"";
  if (defined($params->{'phone_secondary'})) { print " value=\"$params->{'phone_secondary'}\""; }
  print "  </td>\n";
  print "</tr>\n";

  print "</table>\n\n";
  
  print "<p>Please check your contact information carefully before submitting the form.</p>\n\n";

  print "<p>\n";
  print "    <input type=\"submit\" value=\"Change Profile\">\n";
  print "    <input type=\"Reset form fields\">\n";
  print "</p>\n";
  print "</form>\n\n";

  print "<p>For inquiries, please contact the project administrator.</p>\n\n";

  print "</div>\n\n";

  print "<script language=\"javascript\">print_footer();</script>\n\n";
  print "</body>\n";
  print "</html>\n";
  print "\n\n";
}

##### End of sub Print_Profile

##### End of sub routines #####

##### End of script #####
