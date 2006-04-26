#==============================================================================
package OSCARS::WBUI::Method::UserModify;

=head1 NAME

OSCARS::WBUI::Method::UserModify - Gets the profile for the given OSCARS user.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserModify;

=head1 DESCRIPTION

Makes a SOAP request to get the given user's profile from the database.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 25, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# postProcess:  Reset the method name so the correct tab is highlighted.
#
sub postProcess {
    my( $self, $params, $results ) = @_;

    $params->{method} = 'UserList';
} #___________________________________________________________________________ 


###############################################################################
# outputDiv:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub outputDiv {
    my( $self, $results, $authorizations ) = @_;

    # may be accessing another user's profile if an administrator
    my $login = $results->{selectedUser} ? $results->{selectedUser} : $results->{login};
    my $modifySubmitStr = "return submit_form(this,
                    'method=UserModify;', check_profile_modification);";
    my $msg = "User profile";
    print( qq{
    <div>
    <h3>Editing profile for user: $login</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$modifySubmitStr">
    <p><input type='submit' value='Modify Profile'></input></p>
    <table>
      <tbody>
      <tr><td>Login Name</td><td>$login</td></tr>
    } );
    $self->outputPasswordFields($results);
    $self->outputProfileFields($results);
    print("</tbody></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# outputProfileFields:  print fields of user profile
#
sub outputProfileFields {
    my( $self, $results ) = @_;

    # take care of non_required fields
    my $description =
        $results->{description} ? $results->{description} : "";
    my $emailSecondary =
        $results->{emailSecondary} ne 'NULL' ? $results->{emailSecondary} : "";
    my $phoneSecondary =
        $results->{phoneSecondary} ne 'NULL' ? $results->{phoneSecondary} : "";

    my $firstName = $results->{firstName};
    my $lastName = $results->{lastName};
    my $institution = $results->{institutionName};
    my $emailPrimary = $results->{emailPrimary};
    my $phonePrimary = $results->{phonePrimary};
    print( qq{
      <tr>
        <td>First Name</td>
        <td><input class='required' type='text' name='firstName'
             size='40' value='$firstName'></input>
        </td>
      </tr>
      <tr>
        <td>Last Name</td>
        <td><input class='required' type='text' name='lastName' 
             size='40' value='$lastName'></input>
        </td>
      </tr>
      <tr>
        <td>Organization</td>
        <td><select class='required' name='institutionName'>
      } );
      my $institutionList = $results->{institutionList};
      for my $row (@$institutionList) {
          print("<option value='$row->{name}' ");
	  if ( $row->{name} eq $institution ) {
              print( "selected='selected'" );
	  }
	  print( ">$row->{name}</option>" );
      }
      print( qq{
          </select>
        </td>
      </tr>
      <tr>
        <td valign='top'>Personal Description</td>
          <td><input class='SOAP' type='text' name='description' size='40'
	     value='$description'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Primary)</td>
        <td><input class='required' type='text' name='emailPrimary'
             size='40' value='$emailPrimary'></input>
        </td>
      </tr>
      <tr>
        <td>E-mail (Secondary)</td>
        <td><input class='SOAP' type='text' name='emailSecondary' size='40'
             value='$emailSecondary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Primary)</td>
        <td><input class='required' type='text' name='phonePrimary'
             size='40' value='$phonePrimary'></input>
        </td>
      </tr>
      <tr>
        <td>Phone Number (Secondary)</td>
        <td><input class='SOAP' type='text' name='phoneSecondary' size='40'
             value='$phoneSecondary'></input>
        </td>
      </tr>
    } );
} #____________________________________________________________________________


###############################################################################
# outputPasswordFields:  print rows having to do with passwords
#
sub outputPasswordFields {
    my( $self, $params ) = @_;

    print( qq{
      <tr>
        <td>New Password (Enter twice )</td>
        <td><input class='SOAP' type='password' name='passwordNewOnce' size='40'></input>
        </td>
      </tr>
      <tr>
        <td>New Password Confirmation</td>
        <td><input class='SOAP' type='password' name='passwordNewTwice' size='40'></input>
        </td>
      </tr>
    } );
} #____________________________________________________________________________
 

######
1;
