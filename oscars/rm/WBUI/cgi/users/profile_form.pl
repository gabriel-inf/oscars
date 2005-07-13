#!/usr/bin/perl -w

# userprofile.pl:  Main service: My Profile page
# Last modified: July 6, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use CGI;

use AAAS::Client::SOAPClient;
use Data::Dumper;

require '../lib/general.pl';

my( %form_params, $oscars_home );

my $cgi = CGI->new();
($form_params{user_dn}, $form_params{user_level}, $oscars_home) =
                                          check_session_status(undef, $cgi);

if (!$form_params{user_dn}) {
    print "Location:  $oscars_home\n\n";
    exit;
}

for $_ ($cgi->param) {
    $form_params{$_} = $cgi->param($_);
}
process_form(\%form_params);
exit;

######

##############################################################################
# process_form:  Make the SOAP call, and print out the results
#
sub process_form {
    my( $form_params ) = @_;

    my( $error_status, $results );

    if ($form_params->{id}) {
        $form_params->{admin_dn} = $form_params->{user_dn};
        $form_params->{user_dn} = $form_params->{id};
    }
    if ($form_params->{new_user_dn}) {
        $form_params->{admin_dn} = $form_params->{user_dn};
        $form_params->{user_dn} = $form_params->{new_user_dn};
    }
    if ($form_params->{set}) { $form_params->{method} = 'soap_set_profile'; }
    else { $form_params->{method} = 'soap_get_profile'; }

    my $som = aaas_dispatcher($form_params);
    if ( $som->faultstring ) {
        update_status_frame(1, $som->faultstring);
        return;
    }
    $results = $som->result;
    print_profile($results, $form_params);
    if ($form_params->{set}) {
        update_status_frame(0, "Successfully updated user profile.");
    }
    else {
        update_status_frame(0, "Successfully retrieved user profile.");
    }
}
######

##############################################################################
# print_profile:  print the user profile retrieved via the SOAP call
#
sub print_profile {
    my( $results, $form_params ) = @_;

    my $rowref = $results->{row};
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
    print '<script language="javascript">print_navigation_bar("', $form_params->{user_level}, '", "profile");</script>', "\n\n";

    print '<div id="account_ui">', "\n\n";

        # This will only happen if coming in from "accounts list" page as admin
    if ($form_params->{admin_dn} && !$form_params->{new_user_dn}) {
        print "<h3>Editing profile for user: $form_params->{user_dn}</h3>\n";
    }
    print '<p>Required fields are marked with an <span class="requiredmark">*</span></p>', "\n";
    print '<form method="post" action="' .  $oscars_home .
          'cgi-bin/users/profile_form.pl" ' .
          'onsubmit="return check_form(this);">' . "\n";
 
    print '<table>', "\n";
    print '<tr>', "\n";

        # this will only happen if administrator
    if ($form_params->{admin_dn}) {
        print '  <th><span class="requiredmark">*</span> Distinguished Name</th>', "\n";
        print '  <td><input type="text" name="user_dn" size="20"', "\n";
        if (defined($form_params->{user_dn})) {
            print " value=\"$form_params->{user_dn}\"";
        }
        print '  </td>', "\n";
    }
    else {
        print '  <th>Distinguished Name</th>', "\n";
        print "  <td>$form_params->{user_dn}</td>\n";
    }
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '   <th><span class="requiredmark">*</span> Current Password</th>', "\n";
    print '   <td><input type="password" name="user_password" size="20"></td>', "\n";
    print '</tr>', "\n";
    print '<tr>', "\n";
    print '   <th>New Password (Enter twice; Leave blank to stay the same)</th>', "\n";
    print '   <td>', "\n";
    print '       <input type="password" name="password_new_once" size="20" value="" style="margin-bottom: .3em"><br>', "\n";
    print '       <input type="password" name="password_new_twice" size="20" value="">', "\n";
    print '   </td>', "\n";
    print '</tr>', "\n";
    if ($form_params->{admin_dn}) {
        print '<tr>', "\n";
        print '   <th><span class="requiredmark">*</span> User Level</th>', "\n";
        print '   <td><input type="text" name="new_user_level" size="20"></td>', "\n";
        print '</tr>', "\n";
    }
    print '</table>', "\n";
    print '<table>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> First Name</th>', "\n";
    print '  <td><input type="text" name="user_first_name" size="20"';
    if (defined($rowref->{user_first_name})) {
        print " value=\"$rowref->{user_first_name}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Last Name</th>', "\n";
    print '  <td><input type="text" name="user_last_name" size="20"';
    if (defined($rowref->{user_last_name})) {
        print " value=\"$rowref->{user_last_name}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Organization</th>', "\n";
    print '  <td><input type="text" name="institution" size="40"';
    if (defined($rowref->{institution})) {
        print " value=\"$rowref->{institution}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th valign="top">Personal Description</th>', "\n";
    print '  <td><textarea name="user_description" rows="3" cols="34">';
    if (defined($rowref->{user_description})) {
         print "$rowref->{user_description}";
    }
    print '  </textarea></td>', "\n";
    print '</tr>', "\n";

    print '</table>', "\n\n";
  
    print '<table>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> E-mail (Primary)</th>', "\n";
    print '  <td><input type="text" name="user_email_primary" size="40"';
    if (defined($rowref->{user_email_primary})) {
        print " value=\"$rowref->{user_email_primary}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>E-mail (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_email_secondary" size="40"';
    if (defined($rowref->{user_email_secondary})) {
        print " value=\"$rowref->{user_email_secondary}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th><span class="requiredmark">*</span> Phone Number (Primary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_primary" size="40"';
    if (defined($rowref->{user_phone_primary})) { 
        print " value=\"$rowref->{user_phone_primary}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '<tr>', "\n";
    print '  <th>Phone Number (Secondary)</th>', "\n";
    print '  <td><input type="text" name="user_phone_secondary" size="40"';
    if (defined($rowref->{user_phone_secondary})) {
        print " value=\"$rowref->{user_phone_secondary}\"";
    }
    print '  </td>', "\n";
    print '</tr>', "\n";

    print '</table>', "\n\n";
  
    print '<p>Please check your contact information carefully before submitting the form.</p>', "\n\n";

    print '<p>', "\n";
    print '    <input type="hidden" name="set" value="1">', "\n";
    print '    <input type="submit" value="Change Profile">', "\n";
    print '</p>', "\n";
    print '</form>', "\n\n";

    print '<p>For inquiries, please contact the project administrator.</p>', "\n\n";

    print '</div>', "\n\n";

    print '<script language="javascript">print_footer();</script>', "\n\n";
    print '</body>', "\n";
    print '</html>', "\n";
    print "\n\n";
}
######

1;
