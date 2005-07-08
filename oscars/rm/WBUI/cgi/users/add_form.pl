#!/usr/bin/perl

# add_form.pl:  Add a user (requires admin privileges)
# Last modified: July 7, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

# include libraries

require '../lib/general.pl';

my( %form_params, $oscars_home );

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}, $oscars_home) =
                                         check_session_status(undef, $cgi);

if (!$form_params{user_level}) {
    print "Location:  $oscars_home\n\n";
    exit;
}
for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
print_add_user_form(\%form_params);
update_status_frame(0, "Creating new user.");
exit;

##############################################################################
# print_add_user_form:  print "add user" form (same as profile_form, except
#                       that there are no fields filled in
#
sub print_add_user_form {
    my( $form_params ) = @_;

    print '<html>', "\n";
    print '<head>', "\n";
    print '<link rel="stylesheet" type="text/css" ';
    print ' href="' . $oscars_home . 'styleSheets/layout.css">' . "\n";
    print '    <script language="javascript" type="text/javascript"' .
          '    src="' . $oscars_home . 'main_common.js"></script>' . "\n";
    print '    <script language="javascript" type="text/javascript" ' .
          '    src="' . $oscars_home . 'userprofile.js"></script>', "\n";
    print '</head>', "\n\n";

    print '<body>', "\n\n";
    print '<script language="javascript">print_navigation_bar("', $form_params->{user_level}, '", "adduser");</script>', "\n\n";

    print '<div id="account_ui">', "\n\n";

    print "<h2>Add a new user (not functional yet)</h2>\n";
    print '<p>Required fields are marked with an <span class="requiredmark">*</span>. ', "\n";
    print "The Admin Password is the password for $form_params{user_dn}.</p>\n";
    print '<form method="post" action="' .  $oscars_home .
          'cgi-bin/users/profile_form.pl" ' .
          'onsubmit="return check_form(this);">' . "\n";
 
    print '<table>', "\n";
    print '<tr>', "\n";

    print '  <th><span class="requiredmark">*</span> Distinguished Name</th>', "\n";
    print '  <td><input type="text" name="user_dn" size="20"', "\n";
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> Admin Password</th>', "\n";
    print '   <td><input type="password" name="user_password" size="20"></td>', "\n";
    print '</tr>', "\n";
    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> New User Password (Enter twice)</th>', "\n";
    print '   <td>', "\n";
    print '       <input type="password" name="password_new_once" size="20" value="" style="margin-bottom: .3em"><br>', "\n";
    print '       <input type="password" name="password_new_twice" size="20" value="">', "\n";
    print '   </td>', "\n";
    print '</tr>', "\n";
    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> User Level</th>', "\n";
    print '   <td><input type="text" name="new_user_level" size="20"></td>', "\n";
    print '</tr>', "\n";
    print '</table>', "\n";

    print '<table>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> First Name</th>', "\n";
    print '  <td><input type="text" name="user_first_name" size="20"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Last Name</th>', "\n";
    print '  <td><input type="text" name="user_last_name" size="20"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Organization</th>', "\n";
    print '  <td><input type="text" name="institution" size="40"';
    print '  </td>', "\n";
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
    print '  <td><input type="text" name="user_email_primary" size="40"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>E-mail (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_email_secondary" size="40"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Phone Number (Primary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_primary" size="40"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>Phone Number (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_secondary" size="40"';
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '</table>', "\n\n";
  
    print '<p>', "\n";
    print '    <input type="hidden" name="set" value="1">', "\n";
    print '    <input type="submit" value="Create Profile">', "\n";
    print '</p>', "\n";
    print '</form>', "\n\n";

    print '</div>', "\n\n";

    print '<script language="javascript">print_footer();</script>', "\n\n";
    print '</body>', "\n";
    print '</html>', "\n";
    print "\n\n";
}
######
######
1;

