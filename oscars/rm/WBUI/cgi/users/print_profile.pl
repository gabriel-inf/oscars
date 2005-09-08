#!/usr/bin/perl -w

# print_profile.pl:   Print profile page.  Used in getting a profile,
#                     updating a profile, and adding a user.
# Last modified: August 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';


##############################################################################
# print_profile:  print user profile form, and resulsts retrieved via a SOAP
#                 call, if any
#
sub print_profile {
    my( $results, $form_params, $starting_page ) = @_;

    my $ctr = 0;
    my $row = $results->{row};

    if ($form_params->{method} ne 'new_user_form') {
        print "<h3>Editing profile for user: $form_params->{user_dn}</h3>\n";
    }
    # This will only happen if coming in from "accounts list" page as admin
    else {
        print "<h3>Add a new user</h3>\n";
        print "<p>The <strong>Admin Password</strong> is your password";
        print " (for <strong>$form_params->{user_dn}</strong>).</p>\n";
    }
    print "<p>Required fields are outlined in green.</p>\n"; 
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
    $ctr = print_dn_fields($form_params, $ctr);
    $ctr = print_password_fields($form_params, $ctr);
    if ($form_params->{admin_dn}) {
        $ctr = start_row($ctr);
        print "<td>User Level</td>",
              "<td><input class=\"required\" type=\"text\" name=\"user_level\" size=\"40\"";
        if (($form_params->{method} ne 'new_user_form') && (defined($form_params->{user_level}))) {
            print " value=\"$form_params->{user_level}\"";
        }
        print "></input></td></tr>\n";
    }

    $ctr = start_row($ctr);
    print "<td>First Name</td>",
        "<td><input class=\"required\" type=\"text\" name=\"user_first_name\" size=\"40\"";
    if (defined($row->{user_first_name})) {
        print " value=\"$row->{user_first_name}\"";
    }
    print "></input></td></tr>\n";

    $ctr = start_row($ctr);
    print "<td>Last Name</td>",
          "<td><input class=\"required\" type=\"text\" name=\"user_last_name\" size=\"40\"";
    if (defined($row->{user_last_name})) {
        print " value=\"$row->{user_last_name}\"";
    }
    print "></input></td></tr>\n";

    $ctr = start_row($ctr);
    print "<td>Organization</td>",
	  "<td><input class=\"required\" type=\"text\" name=\"institution\" size=\"40\"";
    if (defined($row->{institution})) {
        print " value=\"$row->{institution}\"";
    }
    print "></input></td></tr>";

    $ctr = start_row($ctr);
    print "<td valign=\"top\">Personal Description</td>",
        "<td><textarea name=\"user_description\" rows=\"3\" cols=\"50\">";
    if (defined($row->{user_description})) {
         print "$row->{user_description}";
    }
    print "</textarea></td></tr>\n";
  
    $ctr = start_row($ctr);
    print "<td>E-mail (Primary)</td>",
          "<td><input class=\"required\" type=\"text\" name=\"user_email_primary\" size=\"40\"";
    if (defined($row->{user_email_primary})) {
        print " value=\"$row->{user_email_primary}\"";
    }
    print "></input></td></tr>\n";

    $ctr = start_row($ctr);
    print "<td>E-mail (Secondary)</td>",
          "<td><input type=\"text\" name=\"user_email_secondary\" size=\"40\"";
    if (defined($row->{user_email_secondary})) {
        print " value=\"$row->{user_email_secondary}\"";
    }
    print "></input></td></tr>\n";

    $ctr = start_row($ctr);
    print "<td>Phone Number (Primary)</td>",
          "<td><input class=\"required\" type=\"text\" name=\"user_phone_primary\" size=\"40\"";
    if (defined($row->{user_phone_primary})) { 
        print " value=\"$row->{user_phone_primary}\"";
    }
    print "></input></td></tr>\n";

    $ctr = start_row($ctr);
    print "<td>Phone Number (Secondary)</td>",
          "<td><input type=\"text\" name=\"user_phone_secondary\" size=\"40\"";
    if (defined($row->{user_phone_secondary})) {
        print " value=\"$row->{user_phone_secondary}\"";
    }
    print "></input></td></tr>\n";

    print "</table>\n";
  
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
    my( $form_params, $ctr ) = @_;

    $ctr = start_row($ctr);
    if ($form_params->{method} eq 'new_user_form') {
        print "  <td>Distinguished Name</td>";
        print "  <td><input type=\"text\" name=\"user_dn\" size=\"40\"";
        print "></input>";
    }
    else {
        print "  <td>Distinguished Name</td>",
              "  <td>$form_params->{user_dn}";
    }
    print "</td></tr>\n";
    return $ctr;
}
######

##############################################################################
# print_password_fields:  print rows having to do with passwords
#
sub print_password_fields {
    my( $form_params, $ctr ) = @_;

    my $admin_form = 0;

    # TODO:  FIX
    if ($form_params->{method} eq 'new_user_form') { $admin_form = 1; }

    $ctr = start_row($ctr);
    print "<td> ";
    if ($admin_form) { print " Admin Password"; }
    else { print " Current Password"; }
    print "</td>";
    print "<td>";
    print "<input class=\"required\" type=\"password\" name=\"user_password\" size=\"40\">";
    print "</input>";
    print "</td>";
    print "</tr>\n";

    $ctr = start_row($ctr);
    print "<td>";
    if ($admin_form) { print "New User Password"; }
    else {
        print "New Password (Enter twice;";
    }
    print "</td>";
    print "<td>";
    print "<input type=\"password\" name=\"password_new_once\" size=\"40\">";
    print "</input>";
    print "</td>";
    print "</tr>\n";

    $ctr = start_row($ctr);
    print "<td> ";
    if ($admin_form) { print " (Enter twice)"; }
    else { print " Leave blank to stay the same)"; }
    print "</td>";
    print "<td>";
    print "<input type=\"password\" name=\"password_new_twice\" size=\"40\">";
    print "</input>";
    print "</td>";
    print "</tr>\n";
    return $ctr;
}
######

######
1;
