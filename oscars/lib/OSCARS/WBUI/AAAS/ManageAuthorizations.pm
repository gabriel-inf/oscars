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
    <p>Select a user to view the user's authorizations.
       Click on a resource and permission, and then 'Add', to add an 
       authorization for a user.</p>
    <form method='post' action=''>
    <table width='90%' class='auth-ui'>
    <tr>
    };
    $self->output_table('Users', $results->{users}, 'user_dn');
    $self->output_table('Roles', $results->{roles}, 'user_dn');
    print qq{
      <td class='auth-ui-td'>
        <table class='auth-ui'>
           <tr><td><input type='button'
                  onclick='return tse_addAuthorization(this);' 
                  value='Assign -&gt;'></input></td></tr>
           <tr><td><input type='button' value='Remove &lt;-'></input></td></tr>
        </table>
      </td>
    };
    $self->output_authorizations($results);
    print qq{ </tr></table></form></div> };
} #____________________________________________________________________________


###############################################################################
# output_authorizations:  output authorizations table
#
sub output_authorizations {
    my( $self, $results ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='Authorizations.Authorizations' class='sortable'>
      <thead><tr><td>Resource</td><td>Permission</td></tr></thead>
      <tbody>
    };
    if ( $results->{id} ) {
        $self->output_auth_pairs($results->{authorizations}->{$results->{id}});
    }
    print qq{ </tbody></table></td> };
} #____________________________________________________________________________


###############################################################################
# output_auth_pairs  output table of resource permission pairs
#
sub output_auth_pairs {
    my( $self, $results ) = @_;

    for my $rkey (sort keys %{$results}) {
        for my $pkey (sort keys %{$results->{$rkey}}) {
            print qq{ <tr><td>$rkey</td><td>$pkey</td></tr> };
        }
    }
} #____________________________________________________________________________


###############################################################################
# output_table:  output table for one component of authorization
#
sub output_table {
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


######
1;
