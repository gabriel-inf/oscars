#!/usr/bin/perl -w

# userprofile.pl:  Main service: My Profile page
# Last modified: May 10, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;
use Data::Dumper;

require 'general.pl';

# names of the fields to be displayed on the screen
my @fields_to_display = ( 'user_last_name', 'user_first_name', 'user_dn', 'user_email_primary', 'user_level', 'user_email_secondary', 'user_phone_primary', 'user_phone_secondary', 'user_description', 'user_register_time', 'user_activation_key', 'institution_id' );


my (%form_params, %results);

my $cgi = CGI->new();
($form_params{'user_dn'}, $form_params{'user_level'}, $form_params{'admin_required'}) = check_session_status(undef, $cgi);

if ($form_params{'user_dn'}) {
    foreach $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    if ($form_params{'id'}) {
        $form_params{'user_dn'} = $form_params{'id'};
    }
    ($error_status, %results) = soap_get_profile(\%form_params, \@fields_to_display);
    if (!$error_status) {
        update_frames($error_status, "main_frame", "", $results{'status_msg'});
        print_profile(\%results, \%form_params);
    }
    else {
        update_frames($error_status, "main_frame", "", $results{'error_msg'});
    }
}
else {
    print "Location:  https://oscars.es.net/\n\n";
}

exit;



sub print_profile
{
    my ($results, $form_params) = @_;

    print "<html>\n";
    print "<head>\n";
    print "<link rel=\"stylesheet\" type=\"text/css\" ";
    print " href=\"https://oscars.es.net/styleSheets/layout.css\">\n";
    print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/main_common.js\"></script>\n";
    print "    <script language=\"javascript\" type=\"text/javascript\" src=\"https://oscars.es.net/userprofile.js\"></script>\n";
    print "</head>\n\n";

    print "<body>\n\n";

    if ($form_params->{'admin_required'}) {
        print "<script language=\"javascript\">print_admin_bar(\"adminprofile\");</script>\n\n";
    }
    else {
        print "<script language=\"javascript\">print_navigation_bar(\"userprofile\");</script>\n\n";
    }

    print "<div id=\"account_ui\">\n\n";

    print "<p><em>View/Edit My Profile</em><br>\n";
    print "(Required fields are marked with a <span class=\"requiredmark\">*</span>)</p>\n";
    print "<form method=\"post\" action=\"https://oscars.es.net/cgi-bin/lib/userprofile.pl\" onsubmit=\"return check_form(this);\">\n";
 
    print "<table>\n";
    print "<tr>\n";

    print "  <th><span class=\"requiredmark\">*</span> Distinguished Name</th>\n";
    print "  <td><input type=\"text\" name=\"user_dn\" size=\"20\"\n";
    if ($form_params->{'user_dn'}) { print " value=\"$form_params->{'user_dn'}\""; }
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
    print "  <td><input type=\"text\" name=\"user_first_name\" size=\"20\"";
    if ($results->{'user_first_name'}) { print " value=\"$results->{'user_first_name'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th><span class=\"requiredmark\">*</span> Last Name</th>\n";
    print "  <td><input type=\"text\" name=\"user_last_name\" size=\"20\"";
    if ($results->{'user_last_name'}) { print " value=\"$results->{'user_last_name'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th><span class=\"requiredmark\">*</span> Organization</th>\n";
    print "  <td><input type=\"text\" name=\"institution\" size=\"40\"";
    if ($results->{'institution'}) { print " value=\"$results->{'institution'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th valign=\"top\">Personal Description</th>\n";
    print "  <td><textarea name=\"user_description\" rows=\"3\" cols=\"34\"";
    if ($results->{'user_description'}) { print " value=\"$results->{'user_description'}\""; }
    print "  </textarea></td>\n";
    print "</tr>\n";

    print "</table>\n\n";
  
    print "<table>\n";

    print "<tr>\n";
    print "  <th><span class=\"requiredmark\">*</span> E-mail (Primary)</th>\n";
    print "  <td><input type=\"text\" name=\"user_email_primary\" size=\"40\"";
    if ($results->{'user_email_primary'}) { print " value=\"$results->{'user_email_primary'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th>E-mail (Secondary)</th>\n";
    print "  <td><input type=\"text\" name=\"user_email_secondary\" size=\"40\"";
    if ($results->{'user_email_secondary'}) { print " value=\"$results->{'user_email_secondary'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th><span class=\"requiredmark\">*</span> Phone Number (Primary)</th>\n";
    print "  <td><input type=\"text\" name=\"user_phone_primary\" size=\"40\"";
    if ($results->{'user_phone_primary'}) { print " value=\"$results->{'user_phone_primary'}\""; }
    print "  </td>\n";
    print "</tr>\n";

    print "<tr>\n";
    print "  <th>Phone Number (Secondary)</th>\n";
    print "  <td><input type=\"text\" name=\"user_phone_secondary\" size=\"40\"";
    if ($results->{'user_phone_secondary'}) { print " value=\"$results->{'user_phone_secondary'}\""; }
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
