#==============================================================================
package OSCARS::WBUI::AAA::UserProfile;

=head1 NAME

OSCARS::WBUI::AAA::UserProfile - Gets the profile for the given OSCARS user.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::UserProfile;

=head1 DESCRIPTION

Makes a SOAP request to get the given user's profile from the database.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 12, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# post_process:  Reset the method name so the correct tab is highlighted.
#
sub post_process {
    my( $self, $params, $results ) = @_;

    $params->{method} = 'ManageUsers';
} #___________________________________________________________________________ 


###############################################################################
# output_div:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub output_div {
    my( $self, $results, $authorizations ) = @_;

    # may be accessing another user's profile if an administrator
    my $login = $results->{selected_user} ? $results->{selected_user} : $results->{user_login};
    my $modify_submit_str = "return submit_form(this,
                    'server=AAA;method=UserProfile;op=modifyProfile;',
		    check_profile_modification);";
    my $msg = "User profile";
    print( qq{
    <div>
    <h3>Editing profile for user: $login</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$modify_submit_str">
    <p><input type='submit' value='Modify Profile'></input></p>
    <table>
      <tbody>
      <tr><td>Login Name</td><td>$login</td></tr>
    } );
    $self->output_password_fields($results);
    $self->output_profile_fields($results);
    print("</tbody></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# output_profile_fields:  print fields of user profile
#
sub output_profile_fields {
    my( $self, $results ) = @_;

    # take care of non_required fields
    my $user_description =
        $results->{user_description} ? $results->{user_description} : "";
    my $user_email_secondary =
        $results->{user_email_secondary} ne 'NULL' ? $results->{user_email_secondary} : "";
    my $user_phone_secondary =
        $results->{user_phone_secondary} ne 'NULL' ? $results->{user_phone_secondary} : "";

    my $first_name = $results->{user_first_name};
    my $last_name = $results->{user_last_name};
    my $institution = $results->{institution_name};
    my $user_email_primary = $results->{user_email_primary};
    my $user_phone_primary = $results->{user_phone_primary};
    print( qq{
      <tr>
        <td>First Name</td>
        <td><input class='required' type='text' name='user_first_name'
             size='40' value='$first_name'></input>
        </td>
      </tr>
      <tr>
        <td>Last Name</td>
        <td><input class='required' type='text' name='user_last_name' 
             size='40' value='$last_name'></input>
        </td>
      </tr>
      <tr>
        <td>Organization</td>
        <td><select class='requiredMenu' name='institution_name'>
      } );
      my $institution_list = $results->{institution_list};
      for my $row (@$institution_list) {
          print("<option value='$row->{institution_name}' ");
	  if ( $row->{institution_name} eq $institution ) {
              print( "selected='selected'" );
	  }
	  print( ">$row->{institution_name}</option>" );
      }
      print( qq{
          </select>
        </td>
      </tr>
      <tr>
        <td valign='top'>Personal Description</td>
          <td><input type='text' name='user_description' size='40'
	     value='$user_description'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Primary)</td>
        <td><input class='required' type='text' name='user_email_primary'
             size='40' value='$user_email_primary'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Secondary)</td>
        <td><input type='text' name='user_email_secondary' size='40'
             value='$user_email_secondary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Primary)</td>
        <td><input class='required' type='text' name='user_phone_primary'
             size='40' value='$user_phone_primary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Secondary)</td>
        <td><input type='text' name='user_phone_secondary' size='40'
             value='$user_phone_secondary'></input>
        </td>
      </tr>
    } );
} #____________________________________________________________________________


###############################################################################
# output_password_fields:  print rows having to do with passwords
#
sub output_password_fields {
    my( $self, $params ) = @_;

    print( qq{
      <tr>
        <td>New Password (Enter twice )</td>
        <td><input type='password' name='password_new_once' size='40'></input>
        </td>
      </tr>
      <tr>
        <td>New Password Confirmation</td>
        <td><input type='password' name='password_new_twice' size='40'></input>
        </td>
      </tr>
    } );
} #____________________________________________________________________________
 

######
1;
