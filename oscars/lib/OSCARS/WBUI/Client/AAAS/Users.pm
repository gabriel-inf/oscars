###############################################################################
package Client::AAAS::Users;

# Handles output of list of users; used by ViewUsers, AddUsers, and
# DeleteUsers
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

#____________________________________________________________________________ 


###############################################################################
# output_users:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output_users {
    my ( $results, $session ) = @_;

    print qq{
    <div>
      <p>Click on the user's last name to view detailed user information.</p>
      <table cellspacing='0' width='90%' class='sortable' id='zebra'>
        <thead><tr>
          <td>Last Name</td>          <td>First Name</td>
          <td>Distinguished Name</td> <td>Level</td>
          <td>Organization</td>       <td>Action</td>
        </tr></thead>
      <tbody>
    };
    for my $row (@$results) { print_user( $row, $session ); }
    print qq{
      </tbody></table>
      </div>;
    };
} #____________________________________________________________________________ 


###############################################################################
# print_user:  print the information for one user
#
sub print_user {
    my( $row, $session ) = @_;

    my $str_level = $session->get_str_level($row->{user_level});
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="new_section('AAAS', 'GetProfile', 'id=$row->{user_dn}');
        return false;">$row->{user_last_name}</a></td>
      <td>$row->{user_first_name}</td> <td>$row->{user_dn}</td>
      <td>$str_level</td>      <td>$row->{institution_id}</td>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="new_section('AAAS', 'DeleteUser', 'id=$row->{user_dn}');
        return false;">Delete</a></td>
    </tr>
    };
}

######
 
######
1;
