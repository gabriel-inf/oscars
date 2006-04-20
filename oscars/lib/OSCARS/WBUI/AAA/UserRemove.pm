#==============================================================================
package OSCARS::WBUI::AAA::UserRemove;

=head1 NAME

OSCARS::WBUI::AAA::UserRemove - Handles managing OSCARS users.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::UserRemove;

=head1 DESCRIPTION

Handles managing OSCARS users.  Requires admin privileges.  From this page
an admin can view a list of users, add or delete users, and modify all
user profiles.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub outputDiv {
    my ( $self, $results, $authorizations ) = @_;

    my $msg = "Successfully read user list.";
    my $users = $results->{list};
    my $addSubmitStr = "return submit_form(this,
                    'component=AAA;method=UserAddForm;');";
    print( qq{
      <div>
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
    print( qq{ </tbody></table></div> } );
    return $msg;
} #____________________________________________________________________________


###############################################################################
# printUser:  print the information for one user
#
sub printUser {
    my( $self, $row ) = @_;

    my $profileHrefStr = "return new_section(
                    'component=AAA;method=UserQuery;selectedUser=$row->{login};');";
    my $deleteHrefStr = "return new_section(
                    'component=AAA;method=UserRemove;selectedUser=$row->{login};');";
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
