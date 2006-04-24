#==============================================================================
package OSCARS::WBUI::Method::UserAddForm;

=head1 NAME

OSCARS::WBUI::Method::UserAddForm - Outputs HTML form for adding a user.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserAddForm;

=head1 DESCRIPTION

Outputs HTML form for adding a user (does not make a SOAP call).

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# modifyParams:  resets method name and adds op name
#
sub modifyParams {
    my( $self ) = @_;

    my $params = $self->SUPER::modifyParams();
    $params->{method} = 'InstitutionList';
    return $params;
} #____________________________________________________________________________


###############################################################################
# outputDiv: print add user form.
#
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    my $submitStr = "return submit_form(this, 'method=UserAdd;',
			                check_add_user);";
    my $msg = "Add User Form";
    print( qq{
    <div>
    <h3>Add a new user</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$submitStr">
    <table>
    <tbody>
    <tr>
      <td>Login Name</td>
      <td><input class='required' type='text' name='selectedUser' size='40'></input></td>
    </tr>
    <tr>
      <td>Password (Enter twice)</td>
      <td><input class='required' type='password' name='passwordNewOnce' 
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Password Confirmation</td>
      <td><input class='required' type='password' name='passwordNewTwice' 
           size='40'></input>
      </td>
    </tr>
 
    <tr>
      <td>First Name</td>
      <td><input class='required' type='text' name='firstName'
           size='40' value=''></input>
      </td>
    </tr>
    <tr>
      <td>Last Name</td>
      <td><input class='required' type='text' name='lastName' size='40'>
          </input>
      </td>
    </tr>
    <tr>
      <td>Organization</td>
      <td><select class='requiredMenu' name='institutionName'>
    } );
    my $institutionList = $results->{institutionList};
    for my $row (@$institutionList) {
        print( "<option value='$row->{institutionName}'>" .
              "$row->{institutionName}</option>" );
    }
    print( qq{
        </select>
      </td>
    </tr>
    <tr>
      <td>Personal Description</td>
      <td><input class='SOAP' type='text' name='description' size='40'></input>
      </td>
    </tr>
    <tr>
      <td>E-mail (Primary)</td>
      <td><input class='required' type='text' name='emailPrimary'
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>E-mail (Secondary)</td>
      <td><input class='SOAP' type='text' name='emailSecondary' size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Phone Number (Primary)</td>
      <td><input class='required' type='text' name='phonePrimary'
           size='40'></input>
      </td>
    </tr>
    <tr>
      <td>Phone Number (Secondary)</td>
      <td><input class='SOAP' type='text' name='phoneSecondary' size='40'></input>
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
