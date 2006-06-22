#==============================================================================
package OSCARS::WBUI::Method::AuthorizationList;

=head1 NAME

OSCARS::WBUI::Method::AuthorizationList - List all authorizations.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::AuthorizationList;

=head1 DESCRIPTION

Lists all OSCARS authorizations.  Requires proper authorization.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  print authorizations form, with response from SOAP call 
#
sub outputDiv {
    my( $self, $request, $response, $userTabAuths ) = @_;

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
    $self->granteeTable('Users', $response->{users}, 'login');
    # No roles at the moment.
    #$self->granteeTable('Roles', $response->{roles}, 'login');
    $self->authorizationsTable($response);
    print("</tr></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# granteeTable:  output table listing users or roles that can be
#     assigned authorizations
#
sub granteeTable {
    my( $self, $headerName, $response, $key ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.$headerName' class='sortable'>
        <thead><tr><td>$headerName</td></tr></thead>
        <tbody>
    } );
    for my $name (sort keys %{$response}) {
        print("<tr><td>$name</td></tr>");
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# authorizationsTable:  output authorizations table
#
sub authorizationsTable {
    my( $self, $response ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.Authorizations' class='sortable'>
      <thead><tr><td>Resource</td><td>Permission</td></tr></thead>
      <tbody>
    } );
    if ( $response->{id} ) {
        my $grantee = $response->{authorizations}->{$response->{id}};
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
