#!/usr/bin/perl -w

# print_profile.pl:   Print profile page.  Used in getting a profile,
#                     updating a profile, and adding a user.
# Last modified: August 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;


##############################################################################
# print_profile:  print user profile form, and resulsts retrieved via a SOAP
#                 call, if any
#
sub print_profile {
    my( $results, $form_params, $starting_page ) = @_;

    my $row = $results->{row};

    print STDERR Dumper($form_params);
    if ($form_params->{method} ne 'new_user_form') {
        print "<h3>Editing profile for user: $form_params->{user_dn}</h3>\n";
    }
    # This will only happen if coming in from "accounts list" page as admin
    else {
        print "<h3>Add a new user</h3>\n";
        print "<p>The <strong>Admin Password</strong> is your password";
        print " (for <strong>$form_params->{user_dn}</strong>).</p>\n";
    }
    print "<p>Required fields are marked with an <span class=\"requiredmark\">*</span></p>\n"; 
    print "<form method=\"post\" action=\"\"";
    if ($form_params->{method} ne 'new_user_form') {
        print " onsubmit=\"return submit_form(this, 'set_profile', "; 
        print "'$starting_page/cgi-bin/users/set_profile.pl');\">\n";
    }
    else {
        print " onsubmit=\"return submit_form(this, 'add_user', "; 
        print "'$starting_page/cgi-bin/users/add_user.pl');\">\n";
        print "<input type=\"hidden\" name=\"admin_dn\" value=\"$form_params->{user_dn}\"></input>\n";
    }
    print "<table>";
    print_dn_fields($form_params);
    print_password_fields($form_params);
    if ($form_params->{admin_dn}) {
        print "<tr>",
              "<th><span class=\"requiredmark\">*</span> User Level</th>",
              "<td><input type=\"text\" name=\"user_level\" size=\"20\"";
        if (($form_params->{method} ne 'new_user_form') && (defined($form_params->{user_level}))) {
            print " value=\"$form_params->{user_level}\"";
        }
        print "   ></input></td></tr>\n";
    }
    print "</table>\n<table><tr>",
        "  <th><span class=\"requiredmark\">*</span> First Name</th>",
        "  <td><input type=\"text\" name=\"user_first_name\" size=\"20\"";
    if (defined($row->{user_first_name})) {
        print " value=\"$row->{user_first_name}\"";
    }
    print "></input></td></tr>\n<tr>",
        "  <th><span class=\"requiredmark\">*</span> Last Name</th>",
        "  <td><input type=\"text\" name=\"user_last_name\" size=\"20\"";
    if (defined($row->{user_last_name})) {
        print " value=\"$row->{user_last_name}\"";
    }
    print "></input></td></tr>\n<tr>",
	  "  <th><span class=\"requiredmark\">*</span> Organization</th>",
	  "  <td><input type=\"text\" name=\"institution\" size=\"40\"";
    if (defined($row->{institution})) {
        print " value=\"$row->{institution}\"";
    }
    print "></input></td></tr><tr>",
        "  <th valign=\"top\">Personal Description</th>",
        "  <td><textarea name=\"user_description\" rows=\"3\" cols=\"34\">";
    if (defined($row->{user_description})) {
         print "$row->{user_description}";
    }
    print "  </textarea></td></tr>\n</table>\n";
  
    print "<table><tr>",
        "  <th><span class=\"requiredmark\">*</span> E-mail (Primary)</th>",
        "  <td><input type=\"text\" name=\"user_email_primary\" size=\"40\"";
    if (defined($row->{user_email_primary})) {
        print " value=\"$row->{user_email_primary}\"";
    }
    print "></input></td></tr>\n<tr>",
        "  <th>E-mail (Secondary)</th>",
        "  <td><input type=\"text\" name=\"user_email_secondary\" size=\"40\"";
    if (defined($row->{user_email_secondary})) {
        print " value=\"$row->{user_email_secondary}\"";
    }
    print "></input></td></tr><tr>\n",
        "  <th><span class=\"requiredmark\">*</span> Phone Number (Primary)</th>",
        "  <td><input type=\"text\" name=\"user_phone_primary\" size=\"40\"";
    if (defined($row->{user_phone_primary})) { 
        print " value=\"$row->{user_phone_primary}\"";
    }
    print "></input></td></tr>\n";

    print "<tr>",
        "  <th>Phone Number (Secondary)</th>",
        "  <td><input type=\"text\" name=\"user_phone_secondary\" size=\"40\"";
    if (defined($row->{user_phone_secondary})) {
        print " value=\"$row->{user_phone_secondary}\"";
    }
    print "></input></td></tr>\n</table>\n";
  
    print "<p>Please check your contact information carefully before submitting the form.</p>";

    print "<p><input type=\"submit\" value=\"";
    if ($form_params->{method} ne 'new_user_form') { print "Change Profile"; }
    else { print "Create Profile"; }
    print "\"></input></p></form>\n",
        "<p>For inquiries, please contact the project administrator.</p>\n";
}
######

##############################################################################
# print_dn_fields:  print rows having to do with user's distinguished name
#
sub print_dn_fields {
    my( $form_params ) = @_;

    print "<tr>";
    if ($form_params->{method} eq 'new_user_form') {
        print "  <th><span class=\"requiredmark\">*</span> Distinguished Name</th>";
        print "  <td><input type=\"text\" name=\"user_dn\" size=\"20\"";
        print "></input>";
    }
    else {
        print "  <th>Distinguished Name</th>",
              "  <td>$form_params->{user_dn}";
    }
    print "</td></tr>\n";
}
######

##############################################################################
# print_password_fields:  print rows having to do with passwords
#
sub print_password_fields {
    my( $form_params ) = @_;

    print "<tr><th>";
    if ($form_params->{method} eq 'new_user_form') {
        print "<span class=\"requiredmark\">*</span> Admin Password</th>\n",
              "<td><input",
              " type=\"password\" name=\"user_password\" size=\"20\">";
        print "</input></td></tr>\n";
        print "<tr>\n";
        print "<th><span class=\"requiredmark\">*</span> New User Password (Enter twice)</th>\n";
    }
    else {
        print "<span class=\"requiredmark\">*</span> Current Password</th>",
            "<td><input",
            " type=\"password\" name=\"user_password\" size=\"20\">",
            "</input></td></tr>\n<tr>",
            "<th>New Password (Enter twice; Leave blank to stay the same)</th>";
    }
    print "<td><input type=\"password\" name=\"password_new_once\" size=\"20\"";
    print " value=\"\" style=\"margin-bottom: .3em\"></input>",
       "       <input type=\"password\" name=\"password_new_twice\" size=\"20\" value=\"\">",
       "</input></td></tr>\n";
}
######

1;
