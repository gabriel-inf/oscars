###############################################################################
package Client::AAAS::GetProfile;

# Handles get_profile form submission
#
# Last modified:  November 22, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'AAAS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


##############################################################################
# output:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub output {
    my( $self, $results ) = @_;

    # using cached authorization level
    my $str_level = $self->{session}->get_str_level($self->{user_level});
    print $self->{cgi}->header(
        -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>User profile</msg>
    <div>
    <h3>Editing profile for user: $self->{user_dn}</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action=''
        onsubmit="return submit_form(this, 'set_profile', '');">
    <table id='zebra'>
      <tbody>
      <tr>
        <td>Distinguished Name</td>
        <td>$self->{user_dn}</td>
      </tr>
    };
    $self->output_password_fields($results);
    if ($self->{session}->authorized($self->{user_level}, 'admin')) {
        print qq{
        <tr>
          <td>User Level</td>
          <td>
            <input class='required' type='text' name='user_level' size='40'
                   value="$str_level"></input>
          </td>
        </tr>
        };
    }
    $self->output_profile_fields($results);
    print qq{
      </tbody>
      </table>
      <p>Please check your contact information carefully before submitting 
      the form.</p>
      <p><input type='submit' value='Change Profile'></input></p>
      </form>
      <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


##############################################################################
# output_profile_fields:  print fields of user profile
#
sub output_profile_fields {
    my( $self, $row ) = @_;

    print qq{
      <tr>
        <td>First Name</td>
        <td>
          <input class='required' type='text' name='user_first_name' size='40'
                 value="$row->{user_first_name}">
          </input>
        </td>
      </tr>
      <tr>
        <td>Last Name</td>
        <td>
          <input class='required' type='text' name='user_last_name' size='40'
                 value="$row->{user_last_name}">
          </input>
        </td>
      </tr>
      <tr>
        <td>Organization</td>
        <td>
          <input class='required' type='text' name='institution' size='40'
                 value="$row->{institution_id}">
          </input>
        </td>
      </tr>
      <tr>
        <td valign='top'>Personal Description</td>
        <td>
          <textarea name='user_description' rows='3' cols='50'>
              $row->{user_description}
          </textarea>
        </td>
      </tr>
      <tr>
        <td>E-mail (Primary)</td>
        <td>
          <input class='required' type='text' name='user_email_primary'
                 size='40' value="$row->{user_email_primary}">
          </input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Secondary)</td>
        <td>
          <input type='text' name='user_email_secondary' size='40'
                 value="$row->{user_email_secondary}">
          </input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Primary)</td>
        <td>
          <input class='required' type='text' name='user_phone_primary'
                 size='40' value="$row->{user_phone_primary}">
           </input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Secondary)</td>
        <td>
          <input type='text' name='user_phone_secondary' size='40'
                 value="$row->{user_phone_secondary}">
          </input>
        </td>
      </tr>
    };
} #____________________________________________________________________________


##############################################################################
# output_password_fields:  print rows having to do with passwords
#
sub output_password_fields {
    my( $self, $params ) = @_;

    print qq{
      <tr>
        <td>Current Password</td>
        <td>
          <input class='required' type='password' name='user_password' 
                 size='40'></input>
        </td>
      </tr>
      <tr>
        <td>New Password (Enter twice )</td>
        <td>
          <input type='password' name='password_new_once' size='40'></input>
        </td>
      </tr>
      <tr>
        <td>(Leave blank to stay the same)</td>
        <td>
          <input type='password' name='password_new_twice' size='40'></input>
        </td>
      </tr>
    };
} #____________________________________________________________________________
 

######
1;
