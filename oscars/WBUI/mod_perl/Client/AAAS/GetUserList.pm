package Client::AAAS::GetUserList;

# Handles get user list form submission
#
# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use CGI;
use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

###############################################################################
# output:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output {
    my ( $self, $params ) = @_;

    print "<xml>\n";
    print qq{
      <msg>Successfully read user list.</msg>
    <div id="zebratable_ui">
      <p>Click on the user's last name to view detailed user information.</p>
      <table cellspacing="0" width="90%" class="sortable" id="userlist">
        <thead><tr>
          <td>Last Name</td>          <td>First Name</td>
          <td>Distinguished Name</td> <td>Level</td>
          <td>Organization</td>       <td>Status</td>
        </tr></thead>
      <tbody>
    };
    for my $row (@$params) { $self->print_row( $row ); }
    print qq{
      </tbody></table>
      </div>";
    };
    print "</xml>\n";
}
######

###############################################################################
# print_row:  print the information for one user
#
sub print_row {
    my( $self, $row ) = @_;

    print qq{
    <tr>
      <td><a href="#" style="/styleSheets/layout.css"
        onclick="new_page(
        '/perl/adapt.pl?method=get_profile;id=$row->{user_dn}');
        return false;">$row->{user_last_name}</a></td>
      <td>$row->{user_first_name}</td> <td>$row->{user_dn}</td>
      <td>$row->{user_level}</td>      <td>$row->{institution_id}</td>
      <td>$row->{user_status}</td>
    </tr>
    };
}

######
 
######
1;
