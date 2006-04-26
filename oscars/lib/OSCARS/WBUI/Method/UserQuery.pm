#==============================================================================
package OSCARS::WBUI::Method::UserQuery;

=head1 NAME

OSCARS::WBUI::Method::UserQuery - Gets the profile for the given OSCARS user.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserQuery;

=head1 DESCRIPTION

Makes a SOAP request to get the given user's profile from the database.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 26, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::Method::UserDetails;

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
    my $details = OSCARS::WBUI::Method::UserDetails->new();
    $details->output( $results );
    print("</tbody></table></form></div>\n");
    return $msg;
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
