#==============================================================================
package OSCARS::WBUI::AAAS::ManageAuthorizations;

=head1 NAME

OSCARS::WBUI::AAAS::ManageAuthorizations - Manage all authorizations.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::ManageAuthorizations;

=head1 DESCRIPTION

Manage all OSCARS authorizations, including the handling of modification,
delete, and addition of authorizations for users.
Requires admin privileges.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 30, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output:  print authorizations form, with results retrieved via
#     SOAP call
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header( -type=>'text/xml');
    print "<xml>\n";
    print qq{ <msg>OSCARS authorizations</msg> };
    $self->{tabs}->output('ManageAuthorizations', $results->{authorizations});
    $self->output_results( $results );
    print "</xml>\n";
} #____________________________________________________________________________


###############################################################################
# output_results:  print authorizations form, with results retrieved 
# via SOAP call
#
sub output_results {
    my( $self, $results ) = @_;

    print qq{
    <div>
    <p>Select a user to view a list of the user's currently valid 
       authorizations, and a list of authorizations that could be added.
       Click on an ungranted resource and permission, and then 'Grant', to add 
       an authorization for a user.  Click on a granted resource and
       permission, and then 'Revoke' to revoke an authorization. </p>
    <form method='post' action=''>
    <table width='90%' class='auth-ui'>
    <tr>
    };
    $self->grantee_table('Users', $results->{users}, 'user_login');
    $self->grantee_table('Roles', $results->{roles}, 'user_login');
    $self->ops_table();
    $self->authorizations_table($results);
    print qq{ </tr></table></form></div> };
} #____________________________________________________________________________


###############################################################################
# grantee_table:  output table listing users or roles that can be
#     assigned authorizations
#
sub grantee_table {
    my( $self, $header_name, $results, $key ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.$header_name' class='sortable'>
        <thead><tr><td>$header_name</td></tr></thead>
        <tbody>
    };
    for my $name (sort keys %{$results}) {
        print qq{ <tr><td>$name</td></tr> };
    }
    print qq{ </tbody></table></td> };
} #____________________________________________________________________________


###############################################################################
# ops_table:  output table listing operations to be performed on
#     permissions (add, delete)
#
sub ops_table {
    my( $self ) = @_;

    print qq{
      <td class='auth-ui-td'>
        <table class='auth-ui'>
           <tr><td><input type='button'
                  onclick='return tse_addAuthorization(this);' 
                  value='Grant -&gt;'></input></td></tr>
           <tr><td><input type='button' value='Revoke &lt;-'></input></td></tr>
        </table>
      </td>
    };
} #____________________________________________________________________________


###############################################################################
# authorizations_table:  output authorizations table
#
sub authorizations_table {
    my( $self, $results ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.Authorizations' class='sortable'>
      <thead><tr><td>Resource</td><td>Permission</td></tr></thead>
      <tbody>
    };
    if ( $results->{id} ) {
        my $grantee = $results->{authorizations}->{$results->{id}};
        for my $rkey (sort keys %{$grantee}) {
            for my $pkey (sort keys %{$grantee->{$rkey}}) {
                print qq{ <tr><td>$rkey</td><td>$pkey</td></tr> };
	    }
	}
    }
    print qq{ </tbody></table></td> };
} #____________________________________________________________________________


######
1;
