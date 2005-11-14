#!/usr/bin/perl -w

# print_profile.pl:   Print profile page.  Used in getting a profile,
#                     updating a profile, and adding a user.
# Last modified:  November 13, 2005
# Soo-yeon Hwang  (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;

#require '../lib/general.pl';


##############################################################################
# print_profile:  print user profile form, and resulsts retrieved via a SOAP
#                 call, if any
#
sub print_profile {
    my( $results, $form_params, $starting_page ) = @_;

    if ($form_params->{method} ne 'new_user_form') {
        print "<h3>Editing profile for user: $form_params->{user_dn}</h3>\n";
    }
    # This will only happen if coming in from "accounts list" page as admin
    else {
        print qq{
          <h3>Add a new user</h3>
          <p>
          The <strong>Admin Password</strong> is your password for 
          <strong>$form_params->{user_dn}</strong>.
          </p>]
        };
    }
    print qq{
    <p>Required fields are outlined in green.</p>
    <form method="post" action=""
    };
    if ($form_params->{method} ne 'new_user_form') {
        print qq{
        onsubmit="return submit_form(this, 'set_profile', 
        '$starting_page/cgi-bin/users/set_profile.pl');">
        };
    }
    else {
        print qq{
        onsubmit="return submit_form(this, 'add_user' 
        '$starting_page/cgi-bin/users/add_user.pl');">
        <input type="hidden" name="admin_dn" value="$form_params->{user_dn}">
        </input>
        };
    }
    print "<table>\n";
    print_dn_fields($form_params);
    print_password_fields($form_params);
    if ($form_params->{admin_dn}) {
        print qq{
        <tr>
        <td>User Level</td>
        <td><input class="required" type="text" name="user_level" size="40"
        };
        if (($form_params->{method} ne 'new_user_form') && (defined($form_params->{user_level}))) {
            print qq{ value="$form_params->{user_level}" };
        }
        print qq{ ></input></td></tr> };
    }

    print qq{
      <tr><td>First Name</td>
      <td><input class="required" type="text" name="user_first_name" size="40"
           value="$results->{user_first_name}"></input>
      </td></tr>
      <tr><td>Last Name</td>
      <td><input class="required" type="text" name="user_last_name" size="40"
           value="$results->{user_last_name}></input>
      </td></tr>
      <tr><td>Organization</td>
      <td><input class="required" type="text" name="institution" size="40"
           value="$results->{institution}"</input>
      </td></tr>
      <tr><td valign="top">Personal Description</td>
      <td><textarea name="user_description" rows="3" cols="50">
           $results->{user_description}</textarea>
      </td></tr>
      <tr><td>E-mail (Primary)</td>
      <td><input class="required" type="text" name="user_email_primary"
           size="40" value="$results->{user_email_primary}"></input>
      </td></tr>
      <tr>
      <td>E-mail (Secondary)</td>
      <td><input type="text" name="user_email_secondary" size="40"
           value="$results->{user_email_secondary}"></input>
      </td></tr>
      <tr><td>Phone Number (Primary)</td>
      <td><input class="required" type="text" name="user_phone_primary"
           size="40" value="$results->{user_phone_primary}"></input>
      </td></tr>
      <tr><td>Phone Number (Secondary)</td>
      <td><input type="text" name="user_phone_secondary" size="40"
           value="$results->{user_phone_secondary}"></input>
      </td></tr>
      </table>
      <p>Please check your contact information carefully before submitting 
      the form.</p>

      <p><input type="submit" value="
    };
    if ($form_params->{method} ne 'new_user_form') { print "Change Profile"; }
    else { print "Create Profile"; }
    print qq{
        "></input></p>
        </form>
        <p>For inquiries, please contact the project administrator.</p>
    };
}
######

##############################################################################
# print_dn_fields:  print rows having to do with user's distinguished name
#
sub print_dn_fields {
    my( $form_params ) = @_;

    print qq{
      <tr>
    };
    if ($form_params->{method} eq 'new_user_form') {
        print qq{
        <td>Distinguished Name</td>
        <td><input type="text" name="user_dn" size="40"</input>
        };
    }
    else {
        print qq{
        <td>Distinguished Name</td>
        <td>$form_params->{user_dn}
        };
    }
    print qq{ </td></tr> };
}
######

##############################################################################
# print_password_fields:  print rows having to do with passwords
#
sub print_password_fields {
    my( $form_params ) = @_;

    my $admin_form = 0;

    # TODO:  FIX
    if ($form_params->{method} eq 'new_user_form') { $admin_form = 1; }

    print qq{ <td> };
    if ($admin_form) { print qq{ Admin Password }; }
    else { print qq{ Current Password  }; }
    print qq{
      </td>
      <td>
        <input class="required" type="password" name="user_password" size="40">
        </input>
      </td>
      </tr>
    };
    print qq{
      <tr>
      <td>
    };
    if ($admin_form) { print qq{ New User Password }; }
    else { print qq{ New Password (Enter twice }; }
    print qq {
      </td>
      <td>
        <input type="password" name="password_new_once" size="40"></input>
      </td>
      </tr>
    };

    print qq{
      <tr>
      <td>
    };
    if ($admin_form) { print qq{ (Enter twice) }; }
    else { print qq{ Leave blank to stay the same) }; }
    print qq {
      </td>
      <td>
        <input type="password" name="password_new_twice" size="40"></input>
      </td>
      </tr>
    };
}
######

######
1;
