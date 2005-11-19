package Client::AAAS::UserProfile;

# Handles add_user, get_profile, and set_profile form submission
#
# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

##############################################################################
# output: print add user form, and results from SOAP call, if any
#
sub output {
    my( $self, $params ) = @_;

    my $params_str;

    print "<xml>\n";
    print qq{
    <msg>Successfully added user $params->{user_dn}</msg>
    <div>
    <h3>Add a new user</h3>
    <p>The <strong>Admin Password</strong> is your password for 
    <strong>$params->{user_dn}</strong>.</p>]
    <p>Required fields are outlined in green.</p>
    <form method="post" action="" onsubmit="return submit_form(this,
              '/perl/adapt.pl?method=add_user', '$params_str');">
    <table>
    <tr>
      <td>Distinguished Name</td>
      <td><input type="text" name="user_dn" size="40"</input></td>
    </tr>
    };
    output_password_fields($params);
    print qq{
    <tr>
      <td>User Level</td>
      <td><input class="required" type="text" name="user_level" size="40"
           value="$params->{user_level}"></input> </td>
    </tr>
    };
    output_user_profile_fields($params);
    print qq{
    <p><input type="submit" value="Create Profile"></input></p>
    </form>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print "</xml>\n";
}
######

##############################################################################
# get_user_profile_output:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub output {
    my( $self, $params ) = @_;

    my $params_str;

    print "<xml>\n";
    print qq{
    <msg>User profile</msg>
    <div>
    <h3>Editing profile for user: $params->{user_dn}</h3>
    <p>Required fields are outlined in green.</p>
    <form method="post" action=""
        onsubmit="return submit_form(this,
                                     '/perl/adapt.pl?method=set_profile',
                                     '$params_str');">
    <table>
    <tr><td>Distinguished Name</td> <td>$params->{user_dn}</td></tr>
    };
    output_password_fields($params);
    if ($params->{admin}) {
        print qq{
        <tr>
          <td>User Level</td>
          <td><input class="required" type="text" name="user_level" size="40"
               value="$params->{user_level}"></input></td>
        </tr>
        };
    }
    output_user_profile_fields($params);
    print qq{
    <p><input type="submit" value="Change Profile"></input></p>
    </form>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print "</xml>\n";
}
######

##############################################################################
# output_user_profile_fields:  print fields of user profile
#
sub output_profile_fields {
    my( $self, $row ) = @_;

    print qq{
      <tr><td>First Name</td>
      <td><input class="required" type="text" name="user_first_name" size="40"
           value="$row->{user_first_name}"></input>
      </td></tr>
      <tr><td>Last Name</td>
      <td><input class="required" type="text" name="user_last_name" size="40"
           value="$row->{user_last_name}></input>
      </td></tr>
      <tr><td>Organization</td>
      <td><input class="required" type="text" name="institution" size="40"
           value="$row->{institution}"</input>
      </td></tr>
      <tr><td valign="top">Personal Description</td>
      <td><textarea name="user_description" rows="3" cols="50">
           $row->{user_description}</textarea>
      </td></tr>
      <tr><td>E-mail (Primary)</td>
      <td><input class="required" type="text" name="user_email_primary"
           size="40" value="$row->{user_email_primary}"></input>
      </td></tr>
      <tr>
      <td>E-mail (Secondary)</td>
      <td><input type="text" name="user_email_secondary" size="40"
           value="$row->{user_email_secondary}"></input>
      </td></tr>
      <tr><td>Phone Number (Primary)</td>
      <td><input class="required" type="text" name="user_phone_primary"
           size="40" value="$row->{user_phone_primary}"></input>
      </td></tr>
      <tr><td>Phone Number (Secondary)</td>
      <td><input type="text" name="user_phone_secondary" size="40"
           value="$row->{user_phone_secondary}"></input>
      </td></tr>
      </table>
      <p>Please check your contact information carefully before submitting 
      the form.</p>
    };
}
######


##############################################################################
# output_password_fields:  print rows having to do with passwords
#
sub output_password_fields {
    my( $self, $params ) = @_;

    my $admin_form = 0;

    # TODO:  FIX
    if ($params->{method} eq 'new_user_form') { $admin_form = 1; }

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
