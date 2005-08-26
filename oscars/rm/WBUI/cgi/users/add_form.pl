#!/usr/bin/perl

# add_form.pl:  Add a user (requires admin privileges)
# Last modified: August 26, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use Data::Dumper;

require '../lib/general.pl';

my( $form_params, $auth, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"account_ui\">\n";
print_add_user_form($form_params, $stearting_page);
print  "</div>\n";
print  "</xml>\n";
exit;


##############################################################################
# print_add_user_form:  print "add user" form (same as profile_form, except
#                       that there are no fields filled in)
#
sub print_add_user_form {
    my( $form_params ) = @_;

    print "<h2>Add a new user (not functional yet)</h2>\n";
    print '<p>Required fields are marked with an <span class="requiredmark">*</span>. ', "\n";
    print "The password for <strong>$form_params->{user_dn}</strong>";
    print " is the <strong>Admin Password</strong>.</p>\n";
    print "<form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, 'profile_form', ";
    print "'$starting_page/cgi-bin/users/profile_form.pl');\">\n";
    print "<input type=\"hidden\" name=\"user_dn\" value=\"$form_params->{user_dn}\"></input>\n";
 
    print '<table>', "\n";
    print '<tr>', "\n";

    print '<th><span class="requiredmark">*</span> Distinguished Name</th>', "\n";
    print '<td><input type="text" name="new_user_dn" size="20">', "\n";
    print '</input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> Admin Password</th>', "\n";
    print '   <td><input type="password" name="user_password" size="20"></input></td>', "\n";
    print '</tr>', "\n";
    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> New User Password (Enter twice)</th>', "\n";
    print '   <td>', "\n";
    print '       <input type="password" name="password_new_once" size="20" value="" style="margin-bottom: .3em"></input>', "\n";
    print '       <input type="password" name="password_new_twice" size="20" value=""></input>', "\n";
    print '   </td>', "\n";
    print '</tr>', "\n";
    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> User Level</th>', "\n";
    print '   <td><input type="text" name="new_user_level" size="20"></input></td>', "\n";
    print '</tr>', "\n";
    print '</table>', "\n";

    print '<table>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> First Name</th>', "\n";
    print '  <td><input type="text" name="user_first_name" size="20">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Last Name</th>', "\n";
    print '  <td><input type="text" name="user_last_name" size="20">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Organization</th>', "\n";
    print '  <td><input type="text" name="institution" size="40">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th valign="top">User Description</th>', "\n";
    print '  <td><textarea name="user_description" rows="3" cols="34">';
    print '  </textarea></td>', "\n";
    print '</tr>', "\n";

    print '</table>', "\n\n";
  
    print '<table>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> E-mail (Primary)</th>', "\n";
    print '  <td><input type="text" name="user_email_primary" size="40">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>E-mail (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_email_secondary" size="40">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Phone Number (Primary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_primary" size="40">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>Phone Number (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_secondary" size="40">';
    print '  </input></td>', "\n";
    print '</tr>', "\n";

    print '</table>', "\n\n";
  
    print '<p>', "\n";
    print '    <input type="hidden" name="set" value="1"></input>', "\n";
    print '    <input type="submit" value="Create Profile"></input>', "\n";
    print '</p>', "\n";
    print '</form>', "\n\n";
}
######

######
1;

