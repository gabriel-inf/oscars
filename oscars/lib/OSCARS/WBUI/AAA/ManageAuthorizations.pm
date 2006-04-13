#==============================================================================
package OSCARS::WBUI::AAA::ManageAuthorizations;

=head1 NAME

OSCARS::WBUI::AAA::ManageAuthorizations - Manage all authorizations.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::ManageAuthorizations;

=head1 DESCRIPTION

Manage all OSCARS authorizations, including the handling of modification,
delete, and addition of authorizations for users.
Requires admin privileges.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 12, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  print authorizations form, with results retrieved 
# via SOAP call
#
sub output_div {
    my( $self, $results, $user_tab_authorizations ) = @_;

    my $msg = "OSCARS authorizations";
    print( qq{
    <div>
    <p>Select a user to view a list of all resource/permission pairs.  The
       user's current authorizations are highlighted in green.  Click on an
       unhighlighted resource/permission pair to grant an authorization to a 
       user.  Click on a highlighted pair to revoke an authorization.</p>
    <form method='post' action=''>
    <table width='90%' class='auth-ui'>
    <tr>
    } );
    $self->grantee_table('Users', $results->{users}, 'user_login');
    # No roles at the moment.
    #$self->grantee_table('Roles', $results->{roles}, 'user_login');
    $self->authorizations_table($results);
    print("</tr></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# grantee_table:  output table listing users or roles that can be
#     assigned authorizations
#
sub grantee_table {
    my( $self, $header_name, $results, $key ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.$header_name' class='sortable'>
        <thead><tr><td>$header_name</td></tr></thead>
        <tbody>
    } );
    for my $name (sort keys %{$results}) {
        print("<tr><td>$name</td></tr>");
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# authorizations_table:  output authorizations table
#
sub authorizations_table {
    my( $self, $results ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.Authorizations' class='sortable'>
      <thead><tr><td>Resource</td><td>Permission</td></tr></thead>
      <tbody>
    } );
    if ( $results->{id} ) {
        my $grantee = $results->{authorizations}->{$results->{id}};
        for my $rkey (sort keys %{$grantee}) {
            for my $pkey (sort keys %{$grantee->{$rkey}}) {
                print("<tr><td>$rkey</td><td>$pkey</td></tr>");
	    }
	}
    }
    print("</tbody></table></td>");
} #____________________________________________________________________________


######
1;
