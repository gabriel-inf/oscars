#==============================================================================
package OSCARS::WBUI::Method::UserRemove;

=head1 NAME

OSCARS::WBUI::Method::UserRemove - Handles managing OSCARS users.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::UserRemove;

=head1 DESCRIPTION

Handles managing OSCARS users.  Requires admin privileges.  From this page
an admin can view a list of users, add or delete users, and modify all
user profiles.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# makeCall:  Make call to remove a user, and then make another call to
#            retrieve the resulting user list
#
sub makeCall {
    my( $self, $params ) = @_;

    my $methodName = $self->{method};
    my $som = $self->docLiteralRequest($methodName, $params);
    my $secondSom = $self->docLiteralRequest('userList', {});
    return $secondSom;
} #___________________________________________________________________________ 


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
# outputContent:  If the caller has admin privileges print a list of 
#      all users returned by the SOAP call
#
# In:  response from SOAP call
# Out: None
#
sub outputContent {
    my ( $self, $request, $response ) = @_;

    my $msg = "Successfully read user list.";
    my $users = $response;
    my $addSubmitStr = "return submitForm(this, 'method=UserAddForm;');";
    print( qq{
      <p>Click on the user's last name to view detailed user information.</p>
      <p><input type='button' 
          onclick="$addSubmitStr" 
          value='Add User'></input></p>
      <table id='Users.Users' cellspacing='0' width='90%' class='sortable'>
        <thead><tr>
          <td>Last Name</td><td>First Name</td><td>Login Name</td>
          <td>Organization</td><td>Phone</td><td>Action</td></tr>
        </thead>
      <tbody>
    } );
    for my $row (@$users) { $self->printUser( $row ); }
    print( qq{ </tbody></table> } );
    return $msg;
} #____________________________________________________________________________


###############################################################################
# printUser:  print the information for one user
#
sub printUser {
    my( $self, $row ) = @_;

    my $profileHrefStr = "return newSection(
                    'method=UserQuery;selectedUser=$row->{login};');";
    my $deleteHrefStr = "return newSection(
                    'method=UserRemove;selectedUser=$row->{login};');";
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$profileHrefStr">
	$row->{lastName}</a></td>

      <td>$row->{firstName}</td> <td>$row->{login}</td>
      <td>$row->{institutionName}</td>
      <td>$row->{phonePrimary}</td>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$deleteHrefStr">DELETE</a></td>
    </tr>
    };
} #____________________________________________________________________________


######
1;
