#!/usr/bin/perl

# acctlist_form.pl:  User List page
# Last modified:     November 13, 2005
# David Robertson    (dwrobertson@lbl.gov)
# Soo-yeon Hwang     (dapi@umich.edu)

use Data::Dumper;

require '../lib/general.pl';

my( $form_params, $starting_page ) = get_params();
if ( !$form_params ) { exit; }

$form_params->{server_name} = 'AAAS';
$form_params->{method} = 'get_userlist';
my $results = get_results($form_params);
if (!$results) { exit; }

print "<xml>";
print "<msg>Successfully read user list</msg>\n";
print_userlist($results, $starting_page);
print "</xml>\n";
exit;
######

###############################################################################
# print_userlist:  If the caller has admin privileges print a list of all users
#                  returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub print_userlist
{
    my ( $results, $starting_page ) = @_;

    print qq{
    <div id="zebratable_ui">
      <p>Click on the user's last name to view detailed user information.</p>
      <table cellspacing="0" width="90%" class="sortable" id="userlist">
        <thead><tr>
          <td>Last Name</td>
          <td>First Name</td>
          <td>Distinguished Name</td>
          <td>Level</td>
          <td>Organization</td>
          <td>Status</td>
        </tr></thead>
      <tbody>
    };
    for my $row (@$results) {
        print_row($row, $starting_page);
    }
    print qq{
      </tbody></table>
      </div>";
    };
}
######

###############################################################################
# print_row:  print the information for one user
#
sub print_row
{
    my( $row ) = @_;

    print qq{
    <tr>
      <td><a href="#" style="$starting_page/styleSheets/layout.css"
        onclick="new_page('get_profile',
        '$starting_page/cgi-bin/users/get_profile.pl?id=$row->{user_dn}');
        return false;">$row->{user_last_name}</a></td>
      <td>$row->{user_first_name}</td>
      <td>$row->{user_dn}</td>
      <td>$row->{user_level}</td>
      <td>$row->{institution_id}</td>
      <td>$row->{user_status}</td>
    </tr>
    };
}
######
