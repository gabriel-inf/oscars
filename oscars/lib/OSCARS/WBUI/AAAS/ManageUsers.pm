#==============================================================================
package OSCARS::WBUI::AAAS::ManageUsers;

=head1 NAME

OSCARS::WBUI::AAAS::ManageUsers - Handles managing OSCARS users.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::ManageUsers;

=head1 DESCRIPTION

Handles managing OSCARS users.  Requires admin privileges.  From this page
an admin can view a list of users, add or delete users, and modify all
user profiles.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 13, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::UserSession;
use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output:  Print the list of all users returned by the SOAP call.
#
# In:  results of SOAP call
# Out: None
#
sub output {
    my ( $self, $results ) = @_;

    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>Successfully read user list.</msg> };
    $self->{tabs}->output('ManageUsers', $results->{authorizations});
    $self->output_users( $results);
    print "</xml>\n";
} #___________________________________________________________________________


###############################################################################
# output_users:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output_users {
    my ( $self, $results ) = @_;

    my $users = $results->{list};
    my $add_submit_str = "return submit_form(this,
                    'server=AAAS;method=AddUserForm;');";
    print qq{
    <div>
      <p>Click on the user's last name to view detailed user information.</p>
      <p><input type='button' 
          onclick="$add_submit_str" 
          value='Add User'></input></p>
      <table id='Users.Users' cellspacing='0' width='90%' class='sortable'>
        <thead><tr>
          <td>Last Name</td><td>First Name</td><td>Distinguished Name</td>
          <td>Organization</td><td>Phone</td><td>Action</td></tr>
        </thead>
      <tbody>
    };
    for my $row (@$users) { $self->print_user( $row ); }
    print qq{ </tbody></table></div>; };
} #____________________________________________________________________________


###############################################################################
# print_user:  print the information for one user
#
sub print_user {
    my( $self, $row ) = @_;

    my $profile_href_str = "return new_section(
                    'server=AAAS;method=UserProfile;selected_user=$row->{user_dn};');";
    my $delete_href_str = "return new_section(
                    'server=AAAS;method=ManageUsers;op=deleteUser;selected_user=$row->{user_dn};');";
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$profile_href_str">
	$row->{user_last_name}</a></td>

      <td>$row->{user_first_name}</td> <td>$row->{user_dn}</td>
      <td>$row->{institution_name}</td>
      <td>$row->{user_phone_primary}</td>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="$delete_href_str">DELETE</a></td>
    </tr>
    };
} #____________________________________________________________________________


######
1;
