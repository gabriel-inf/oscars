###############################################################################
package Client::AAAS::ViewUsers;

# Handles get user list form submission
#
# Last modified:  November 22, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::UserSession;
use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#____________________________________________________________________________ 


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'AAAS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# output:  If the caller has admin privileges print a list of 
#          all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub output {
    my ( $self, $results ) = @_;

    print $self->{cgi}->header(
         -type=>'text/xml');
    print "<xml>\n";
    print qq{
      <msg>Successfully read user list.</msg>
    <div>
      <p>Click on the user's last name to view detailed user information.</p>
      <table cellspacing='0' width='90%' class='sortable' id='zebra'>
        <thead><tr>
          <td>Last Name</td>          <td>First Name</td>
          <td>Distinguished Name</td> <td>Level</td>
          <td>Organization</td>       <td>Status</td>
        </tr></thead>
      <tbody>
    };
    for my $row (@$results) { $self->print_row( $row ); }
    print qq{
      </tbody></table>
      </div>;
    };
    print "</xml>\n";
} #____________________________________________________________________________ 


###############################################################################
# print_row:  print the information for one user
#
sub print_row {
    my( $self, $row ) = @_;

    my $str_level = $self->{session}->get_str_level($row->{user_level});
    print qq{
    <tr>
      <td><a href='#' style='/styleSheets/layout.css'
        onclick="new_section('get_profile', 'id=$row->{user_dn}');
        return false;">$row->{user_last_name}</a></td>
      <td>$row->{user_first_name}</td> <td>$row->{user_dn}</td>
      <td>$str_level</td>      <td>$row->{institution_id}</td>
      <td>$row->{user_status}</td>
    </tr>
    };
}

######
 
######
1;
