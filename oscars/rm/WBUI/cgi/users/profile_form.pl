#!/usr/bin/perl -w

# userprofile.pl:  Main service: My Profile page
# Last modified: August 26, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

require '../lib/general.pl';

my( $form_params, $auth, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

if ($form_params->{id}) {
    $form_params->{admin_dn} = $form_params->{user_dn};
    $form_params->{user_dn} = $form_params->{id};
}
if ($form_params->{new_user_dn}) {
    $form_params->{admin_dn} = $form_params->{user_dn};
    $form_params->{user_dn} = $form_params->{new_user_dn};
}
my $results;
if ($form_params->{set}) {
    $results = get_results($form_params, 'set_profile');
}
else {
    $results = get_results($form_params, 'get_profile');
}
if (!$results) { exit; }

print "<xml>\n";
print "<msg>User profile</msg>\n";
print "<div id=\"account_ui\">\n";
print_profile($results, $form_params, $starting_page);
print  "</div>\n";
print  "</xml>\n";
exit;
######

##############################################################################
# print_profile:  build up string for user profile retrieved via the SOAP call
#
sub print_profile {
    my( $results, $form_params ) = @_;

    my $rowref = $results->{row};

        # This will only happen if coming in from "accounts list" page as admin
    if ($form_params->{admin_dn} && !$form_params->{new_user_dn}) {
        print "<h3>Editing profile for user: $form_params->{user_dn}</h3>\n";
    }
    print "<p>Required fields are marked with an <span class=\"requiredmark\">*</span></p>\n"; 
    print "<form method=\"post\" action=\"\"";
    print " onsubmit=\"return submit_form(this, 'profile_form', "; 
    print "'$starting_page/cgi-bin/users/profile_form.pl');\">\n";
    print '<table><tr>';

        # this will only happen if user is administrator
    if ($form_params->{admin_dn}) {
        print '  <th><span class="requiredmark">*</span> Distinguished Name</th>';
        print '  <td><input type="text" name="user_dn" size="20"';
	if (defined($form_params->{user_dn})) {
            print " value=\"$form_params->{user_dn}\"";
        }
        print '></input></td>';
    }
    else {
        print '  <th>Distinguished Name</th>',
              "  <td>$form_params->{user_dn}</td>";
    }
    print "</tr>\n<tr>",
        '   <th><span class="requiredmark">*</span> Current Password</th>',
        '   <td><input type="password" name="user_password" size="20"></input></td>',
        "   </tr>\n<tr>",
        '   <th>New Password (Enter twice; Leave blank to stay the same)</th>',
        '   <td>',
        '       <input type="password" name="password_new_once" size="20" value="" style="margin-bottom: .3em"></input>',
       '       <input type="password" name="password_new_twice" size="20" value=""></input>',
       "</td></tr>\n";
    if ($form_params->{admin_dn}) {
        print '<tr>',
            '   <th><span class="requiredmark">*</span> User Level</th>',
            '   <td><input type="text" name="new_user_level" size="20"></input></td>',
            "</tr>\n";
    }
    print "</table>\n<table><tr>",
        '  <th><span class="requiredmark">*</span> First Name</th>',
        '  <td><input type="text" name="user_first_name" size="20"' ;
    if (defined($rowref->{user_first_name})) {
        print " value=\"$rowref->{user_first_name}\"";
    }
    print "></input></td></tr>\n<tr>",
        '  <th><span class="requiredmark">*</span> Last Name</th>',
        '  <td><input type="text" name="user_last_name" size="20"';
    if (defined($rowref->{user_last_name})) {
        print " value=\"$rowref->{user_last_name}\"";
    }
    print "></input></td></tr>\n<tr>",
        '  <th><span class="requiredmark">*</span> Organization</th>',
        '  <td><input type="text" name="institution" size="40"';
    if (defined($rowref->{institution})) {
        print " value=\"$rowref->{institution}\"";
    }
    print "></input></td></tr><tr>",
        '  <th valign="top">Personal Description</th>',
        '  <td><textarea name="user_description" rows="3" cols="34">';
    if (defined($rowref->{user_description})) {
         print "$rowref->{user_description}";
    }
    print "  </textarea></td></tr>\n</table>\n";
  
    print '<table>',
        '<tr>',
        '  <th><span class="requiredmark">*</span> E-mail (Primary)</th>',
        '  <td><input type="text" name="user_email_primary" size="40"';
    if (defined($rowref->{user_email_primary})) {
        print " value=\"$rowref->{user_email_primary}\"";
    }
    print "></input></td></tr>\n<tr>",
        '  <th>E-mail (Secondary)</th>',
        '  <td><input type="text" name="user_email_secondary" size="40"';
    if (defined($rowref->{user_email_secondary})) {
        print " value=\"$rowref->{user_email_secondary}\"";
    }
    print "></input></td></tr>\n",
        '<tr>',
        '  <th><span class="requiredmark">*</span> Phone Number (Primary)</th>',
        '  <td><input type="text" name="user_phone_primary" size="40"';
    if (defined($rowref->{user_phone_primary})) { 
        print " value=\"$rowref->{user_phone_primary}\"";
    }
    print "></input></td></tr>\n";

    print '<tr>',
        '  <th>Phone Number (Secondary)</th>',
        '  <td><input type="text" name="user_phone_secondary" size="40"';
    if (defined($rowref->{user_phone_secondary})) {
        print " value=\"$rowref->{user_phone_secondary}\"";
    }
    print "></input></td></tr>\n</table>\n";
  
    print '<p>Please check your contact information carefully before submitting the form.</p>';

    print "<p>\n",
        '    <input type="hidden" name="set" value="1"></input>',
        '    <input type="submit" value="Change Profile"></input>',
        "</p></form>\n",
        '<p>For inquiries, please contact the project administrator.</p>';
}
######

1;
