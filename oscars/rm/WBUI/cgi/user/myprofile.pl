#!/usr/bin/perl -w

# myprofile.pl:  Main service: My Profile page
# Last modified: April 20, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

# include libraries
require '../lib/general.pl';
require 'soapclient.pl';

print_profile({});

##### Beginning of sub routines #####


sub print_profile
{
  my ($params) = @_;

  print "Content-type: text/html\n\n";
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
  print "<input type=\"hidden\" name=\"mode\" value=\"updatemyprofile\">\n\n";
 
  print "<table>\n";
  print "<tr>\n";
  print "	  <th><span class=\"requiredmark\">*</span> Distinguished Name</th>\n";
  print "   <td><input type=\"text\" name=\"dn\" size=\"20\"</td>\n";
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
  print "	  <th><span class=\"requiredmark\">*</span> First Name</th>\n";
  print "   <td><input type=\"text\" name=\"firstname\" size=\"20\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "	  <th><span class=\"requiredmark\">*</span> Last Name</th>\n";
  print "	  <td><input type=\"text\" name=\"lastname\" size=\"20\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "	  <th><span class=\"requiredmark\">*</span> Organization</th>\n";
  print "   <td><input type=\"text\" name=\"organization\" size=\"40\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "	  <th valign=\"top\">Personal Description</th>\n";
  print "   <td><textarea name=\"description\" rows=\"3\" cols=\"34\"></textarea></td>\n";
  print "</tr>\n";
  print "</table>\n\n";
  
  print "<table>\n";
  print "<tr>\n";
  print "    <th><span class=\"requiredmark\">*</span> E-mail (Primary)</th>\n";
  print "    <td><input type=\"text\" name=\"email_primary\" size=\"40\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "    <th>E-mail (Secondary)</th>\n";
  print "    <td><input type=\"text\" name=\"email_secondary\" size=\"40\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "    <th><span class=\"requiredmark\">*</span> Phone Number (Primary)</th>\n";
  print "    <td><input type=\"text\" name=\"phone_primary\" size=\"40\"></td>\n";
  print "</tr>\n";
  print "<tr>\n";
  print "    <th>Phone Number (Secondary)</th>\n";
  print "    <td><input type=\"text\" name=\"phone_secondary\" size=\"40\"></td>\n";
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
