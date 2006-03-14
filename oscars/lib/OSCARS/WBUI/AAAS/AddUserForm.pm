#==============================================================================
package OSCARS::WBUI::AAAS::AddUserForm;

=head1 NAME

OSCARS::WBUI::AAAS::AddUserForm - Outputs HTML form for adding a user.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::AddUserForm;

=head1 DESCRIPTION

Outputs HTML form for adding a user (does not make a SOAP call).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 13, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output:  resets method name and adds op name
#
sub modify_params {
    my( $self, $params ) = @_;

    $self->SUPER::modify_params($params);
    $params->{method} = 'ManageUsers';
    $params->{op} = 'addUserForm';
} #____________________________________________________________________________


###############################################################################
# output: print add user form.  Note that the space is important between the 
# starting and ending textarea tags.
#
sub output {
    my( $self, $results ) = @_;

    my $submit_str = "return submit_form(this,
                               'server=AAAS;method=ManageUsers;op=addUser;',
			       check_add_user);";
    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>Add User Form</msg> };
    $self->{tabs}->output('ManageUsers', $results->{authorizations});
    print qq{
    <div>
    <h3>Add a new user</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$submit_str">
    <table>
    <tbody>
    <tr>
      <td>Login Name</td>
      <td><input class='required' type='text' name='selected_user' size='40'></input></td>
    </tr>
    <tr>
      <td>Password (Enter twice)</td>
      <td><input class='required' type='password' name='password_new_once' 
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Password Confirmation</td>
      <td><input class='required' type='password' name='password_new_twice' 
           size='40'></input>
      </td>
    </tr>
 
    <tr>
      <td>First Name</td>
      <td><input class='required' type='text' name='user_first_name' size='40'
           value=''></input>
      </td>
    </tr>
    <tr>
      <td>Last Name</td>
      <td><input class='required' type='text' name='user_last_name' size='40'>
          </input>
      </td>
    </tr>
    <tr>
      <td>Organization</td>
      <td><select class='requiredMenu' name='institution_name'>
    };
    my $institution_list = $results->{institution_list};
    for my $row (@$institution_list) {
        print "<option value='$row->{institution_name}'>" .
              "$row->{institution_name}</option>";
    }
    print qq{
        </select>
      </td>
    </tr>
    <tr>
      <td>Personal Description</td>
      <td><textarea name='user_description' rows='3' cols='50'> </textarea>
      </td>
    </tr>
    <tr>
      <td>E-mail (Primary)</td>
      <td><input class='required' type='text' name='user_email_primary'
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>E-mail (Secondary)</td>
      <td><input type='text' name='user_email_secondary' size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Phone Number (Primary)</td>
      <td><input class='required' type='text' name='user_phone_primary'
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Phone Number (Secondary)</td>
      <td><input type='text' name='user_phone_secondary' size='40'></input>
      </td>
    </tr>
    </tbody>
    </table>

    <p><input type='submit' value='Add User'></input></p>
    </form>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
