###############################################################################
package Client::AAAS::AddUserForm;

# Handles add_user form output
#
# Last modified:  December 13, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# make_call:  make SOAP calls to get institutions and permissions
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    my $results = {};

    $soap_params->{method} = 'ViewInstitutions';
    my $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        $self->update_status_msg($som->faultstring);
        return undef;
    }
    $results->{institutions} = $som->result;
    $soap_params->{method} = 'ViewPermissions';
    $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        $self->update_status_msg($som->faultstring);
        return undef;
    }
    $results->{permissions} = $som->result;
    return $results;
} #___________________________________________________________________________                                         


###############################################################################
# output: print add user form, using institution names from SOAP call
#     to fill in institution menu
#
sub output {
    my( $self, $params ) = @_;

    print $self->{cgi}->header(
         -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>Add User Form</msg>
    <div>
    <h3>Add a new user</h3>
    <p>The <strong>Admin Password</strong> is your password for 
    <strong>$self->{user_dn}</strong>.</p>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="return submit_form(this,
                                                     'AAAS', 'AddUser', '');">
    <table id='zebra'>
    <tbody>
    <tr>
      <td>Distinguished Name</td>
      <td>
        <input class='required' type='text' name='id' size='40'></input>
      </td>
    </tr>
    };
    $self->output_password_fields();
    $self->output_profile_fields( $params );

    print qq{
    </tbody>
    </table>

    <p>Please check your contact information carefully before submitting 
    the form.</p>
    <p><input type='submit' value='Create Profile'></input></p>
    </form>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


##############################################################################
# output_profile_fields:  print fields of user profile
#     Note that the space is important between the starting and ending 
#     textarea tags.
#
sub output_profile_fields {
    my( $self, $params ) = @_;

    my( $iname );

    print qq{
      <tr>
        <td>First Name</td>
        <td>
          <input class='required' type='text' name='user_first_name' size='40'
                 value=''></input>
        </td>
      </tr>
      <tr>
        <td>Last Name</td>
        <td>
          <input class='required' type='text' name='user_last_name' size='40'>
          </input>
         </td>
      </tr>
      <tr>
        <td>Organization</td>
        <td>
          <select class='requiredMenu' name='institution'>
    };
    for my $irow (@{$params->{institutions}}) {
        $iname = $irow->{institution_name};
        print "<option value='$iname'>$iname</option>";
    }
    print qq{
          </select>
        </td>
      </tr>
      <tr>
        <td>Permissions</td>
        <td><select class='requiredMenu' name='permissions' multiple='1'>
    };
    for my $irow (@{$params->{permissions}}) {
        $iname = $irow->{user_level_description};
        print "<option value='$iname'>$iname</option>";
    }
    print qq{
        </select>
        </td>
      </tr>
      <tr>
        <td>Personal Description</td>
        <td>
          <textarea name='user_description' rows='3' cols='50'> </textarea>
        </td>
      </tr>
      <tr>
        <td>E-mail (Primary)</td>
        <td>
          <input class='required' type='text' name='user_email_primary'
                 size='40'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Secondary)</td>
        <td>
          <input type='text' name='user_email_secondary' size='40'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Primary)</td>
        <td>
          <input class='required' type='text' name='user_phone_primary'
                 size='40'>
          </input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Secondary)</td>
        <td>
          <input type='text' name='user_phone_secondary' size='40'></input>
        </td>
      </tr>
    };
} #____________________________________________________________________________


##############################################################################
# output_password_fields:  print rows having to do with passwords
#
sub output_password_fields {
    my( $self ) = @_;

    print qq{
      <tr>
        <td>Admin Password</td>
        <td>
          <input class='required' type='password' name='user_password' 
                 size='40'>
          </input>
         </td>
      </tr>
      <tr>
        <td>New User Password</td>
        <td>
          <input class='required' type='password' name='password_new_once' 
                 size='40'>
          </input>
        </td>
      </tr>
      <tr>
        <td>(Enter twice)</td>
        <td>
          <input class='required' type='password' name='password_new_twice' 
                 size='40'>
          </input>
        </td>
      </tr>
    };
} #____________________________________________________________________________
 

######
1;
