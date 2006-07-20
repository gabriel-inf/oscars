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

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::UserDetails;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'UserList';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  print user profile form, and response retrieved via
# a SOAP call, if any
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    # may be accessing another user's profile if an administrator
    my $login = $response->{selectedUser} ? $response->{selectedUser} : $request->{login};
    my $modifySubmitStr = "return submitForm(this,
                    'method=UserModify;', checkProfileModification);";
    my $msg = "User profile";
    print( qq{
    <h3>Editing profile for user: $login</h3>
    <p>Required fields are outlined in green.</p>
    <form method='post' action='' onsubmit="$modifySubmitStr">
    <p><input type='submit' value='Modify Profile'></input></p>
    <table>
      <tbody>
      <tr><td>Login Name</td><td>$login</td></tr>
    } );
    $self->outputPasswordFields($response);
    my $details = OSCARS::WBUI::Method::UserDetails->new();
    $details->output( $request, $response );
    print("</tbody></table></form>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# outputPasswordFields:  print rows having to do with passwords
#
sub outputPasswordFields {
    my( $self, $request ) = @_;

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
