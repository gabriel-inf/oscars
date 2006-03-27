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

March 26, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# modify_params:  resets method name and adds op name
#
sub modify_params {
    my( $self ) = @_;

    my $params = $self->SUPER::modify_params();
    $params->{method} = 'ManageUsers';
    $params->{op} = 'addUserForm';
    return $params;
} #____________________________________________________________________________


###############################################################################
# output_div: print add user form.
#
sub output_div {
    my( $self, $results, $authorized ) = @_;

    my $submit_str = "return submit_form(this,
                               'server=AAAS;method=ManageUsers;op=addUser;',
			       check_add_user);";
    my $msg = "Add User Form";
    print( qq{
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
      <td><input class='required' type='text' name='user_first_name'
           size='40' value=''></input>
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
    } );
    my $institution_list = $results->{institution_list};
    for my $row (@$institution_list) {
        print( "<option value='$row->{institution_name}'>" .
              "$row->{institution_name}</option>" );
    }
    print( qq{
        </select>
      </td>
    </tr>
    <tr>
      <td>Personal Description</td>
      <td><input type='text' name='user_description' size='40'></input>
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
    </form></div>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
