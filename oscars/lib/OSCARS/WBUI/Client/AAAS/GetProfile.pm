###############################################################################
package Client::AAAS::GetProfile;

# Handles get_profile form submission
#
# Last modified:  December 7, 2005
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


###############################################################################
# make_call:  make SOAP calls to get profile, and get permissions list
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    # First make call to get profile (any exceptions are handled there)
    my $results = $self->SUPER::make_call($soap_server, $soap_params);

    # and then get back list of permissions for use in permissions menu
    # (if admin)
    if ($self->{session}->authorized($self->{user_level}, 'admin')) {
        $soap_params->{method} = 'view_permissions';
        my $som = $soap_server->dispatch($soap_params);
        if ($som->faultstring) {
            $self->update_status_msg($som->faultstring);
            return undef;
        }
        $self->{permissions_list} = $som->result;
    }
    return $results;
} #____________________________________________________________________________


##############################################################################
# output:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub output {
    my( $self, $results ) = @_;

    my( $str_level );

    # use cached authorization level if not admin
    if ($self->{session}->authorized($self->{user_level}, 'admin')) {
        $str_level = $self->{session}->get_str_level($results->{user_level});
    }
    else {
        $str_level = $self->{session}->get_str_level($self->{user_level});
    }
    # may be accessing another user's profile if an administrator
    my $dn = $results->{id} ? $results->{id} : $self->{user_dn};
    print $self->{cgi}->header(
        -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>User profile</msg>
    <div>
    <h3>Editing profile for user: $dn</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action=''
        onsubmit="return submit_form(this, 'set_profile', '');">
    <table id='zebra'>
      <tbody>
      <tr>
        <td>Distinguished Name</td>
        <td>$dn</td>
      </tr>
    };
    $self->output_password_fields($results);
    $self->output_profile_fields($results, $str_level);
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
    my( $self, $row, $str_level ) = @_;

    # take care of non_required fields
    my $user_description =
        $row->{user_description} ? $row->{user_description} : "";
    my $user_email_secondary =
        $row->{user_email_secondary} ? $row->{user_email_secondary} : "";
    my $user_phone_secondary =
        $row->{user_phone_secondary} ? $row->{user_phone_secondary} : "";
    if ($self->{session}->authorized($self->{user_level}, 'admin')) {
        print qq{
          <tr>
            <td>Permissions</td>
            <td><select class='requiredMenu' name='permissions' multiple='multiple' size='3'>
        };
        my @fields = split(' ', $str_level);
        my %user_permissions;
        my %all_permissions;
        for $_ (@fields) {
            $user_permissions{$_} = 1;
        }
        for $_ (@{$self->{permissions_list}}) {
            $all_permissions{$_->{user_level_description}} = 1;
        }
        for $_ (keys(%all_permissions)) {
            if ($user_permissions{$_}) {
                print "<option value='$_' selected='yes'>$_</option>";
            }
            else { print "<option value='$_'>$_</option>"; }
        }
        print qq{
        </select>
        </td>
        </tr>
        }
      }
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
              $user_description
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
                 value="$user_email_secondary">
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
                 value="$user_phone_secondary">
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
