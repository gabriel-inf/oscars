#==============================================================================
package OSCARS::WBUI::AAA::ManageUsers;

=head1 NAME

OSCARS::WBUI::AAA::ManageUsers - Handles managing OSCARS users.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::ManageUsers;

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
# output_div:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output_div {
    my ( $self, $results, $authorizations ) = @_;

    my $msg = "Successfully read user list.";
    my $users = $results->{list};
    my $add_submit_str = "return submit_form(this,
                    'component=AAA;method=AddUserForm;');";
    print( qq{
      <div>
      <p>Click on the user's last name to view detailed user information.</p>
      <p><input type='button' 
          onclick="$add_submit_str" 
          value='Add User'></input></p>
      <table id='Users.Users' cellspacing='0' width='90%' class='sortable'>
        <thead><tr>
          <td>Last Name</td><td>First Name</td><td>Login Name</td>
          <td>Organization</td><td>Phone</td><td>Action</td></tr>
        </thead>
      <tbody>
    } );
    for my $row (@$users) { $self->print_user( $row ); }
    print( qq{ </tbody></table></div> } );
    return $msg;
} #____________________________________________________________________________


###############################################################################
# print_user:  print the information for one user
#
sub print_user {
    my( $self, $row ) = @_;

    my $profile_href_str = "return new_section(
                    'component=AAA;method=UserProfile;op=viewProfile;selected_user=$row->{user_login};');";
    my $delete_href_str = "return new_section(
                    'component=AAA;method=ManageUsers;op=deleteUser;selected_user=$row->{user_login};');";
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$profile_href_str">
	$row->{user_last_name}</a></td>

      <td>$row->{user_first_name}</td> <td>$row->{user_login}</td>
      <td>$row->{institution_name}</td>
      <td>$row->{user_phone_primary}</td>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$delete_href_str">DELETE</a></td>
    </tr>
    };
} #____________________________________________________________________________


######
1;
