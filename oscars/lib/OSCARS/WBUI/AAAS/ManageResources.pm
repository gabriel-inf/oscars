#==============================================================================
package OSCARS::WBUI::AAAS::ManageResources;

=head1 NAME

OSCARS::WBUI::AAAS::ManageResources - Manage all resources.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::ManageResources;

=head1 DESCRIPTION

Manage all OSCARS resources, including the handling of modification,
delete, and addition of resources and permissions.  Requires admin privileges.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 29, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output:  print resources form, with results retrieved via SOAP call
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>OSCARS resources</msg> };
    $self->{tabs}->output('ManageResources', $results->{authorizations});
    $self->output_resource_entry();
    $self->output_resources( $results );
    print "</xml>\n";
} #____________________________________________________________________________


###############################################################################
# output_resource_entry: print form to enter new resource.  Note that there
#     must be a space between textarea elements.
#
sub output_resource_entry {
    my( $self ) = @_;

    my $add_submit_str = "return submit_form(this,
                    'server=AAAS;method=ManageResources;op=addResource;');";
    my $delete_submit_str = "return submit_form(this,
                    'server=AAAS;method=ManageResources;op=deleteResource;');";
    print qq{
    <td>
    <form method='post' action=''>
    <table class='sortable'>
    <tbody>
    <tr>
      <td>Resource Name</td>
      <td><input class='required' type='text' name='resource_name' size='40'
           value=''></input></td>
    </tr>
    <tr>
      <td>Resource Description</td>
      <td><textarea name='resource_description' rows='3' cols='50'> </textarea>
      </td>
    </tr>
    </tbody>
    </table>

    <p><input type='submit' value='Add Resource'></input></p>
    </form>
    </td>
    <td class='auth-ui-td'>
    <table class='auth-ui'>
	<tr><td><input type='button'
                onclick='$add_submit_str'; 
                value='Add -&gt;'></input></td></tr>
        <tr><td><input type='button' 
                onclick='$delete_submit_str' 
	        value='Delete &lt;-'></input></td></tr>
    </table>
    </td>
    };
} #____________________________________________________________________________


###############################################################################
# output_resources:  print resources form, with results retrieved via SOAP call
#
sub output_resources {
    my( $self, $results ) = @_;

    print qq{
    <div>
    <p>Click on a resource and permission, and then 'Add', to add a permission required to use a resource.</p>
    <form method='post' action='' onsubmit="return submit_form(this,
                                      'server=AAAS;method=ManageResources;');">
    <table width='90%' class='auth-ui'>
    <tr>
    };
    $self->output_table('Resources', $results->{resources}, 'resource_name');
    $self->output_table('Permissions', $results->{permissions},
                        'permission_name');
    print qq{
      <td class='auth-ui-td'>
        <table class='auth-ui'>
            <tr><td><input type='button' value='Add -&gt;'></input></td></tr>
            <tr><td><input type='button' value='Delete &lt;-'></input></td>
            </tr>
        </table>
      </td>
    };
    $self->output_resource_permissions($results->{resource_permissions});
    print qq{ </tr></table></form></div>
    };
} #____________________________________________________________________________


###############################################################################
# output_resource_permissions:  output resource permissions table
#
sub output_resource_permissions {
    my( $self, $results ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='Resources.ResourcePermissions' class='sortable'>
        <thead><tr><td>Resource</td><td>Requires</td></tr></thead>
        <tbody>
    };
    $self->output_result_perm_pairs($results);
    print qq{ </tbody></table></td> };
} #____________________________________________________________________________


###############################################################################
# output_result_perm_pairs  output table of resource permission pairs
#
sub output_result_perm_pairs {
    my( $self, $results ) = @_;

    for my $rkey (sort keys %{$results}) {
        for my $pkey (sort keys %{$results->{$rkey}}) {
            print qq{ <tr><td>$rkey</td><td>$pkey</td></tr> };
        }
    }
} #____________________________________________________________________________


###############################################################################
# output_table:  output table for one component of resources (deletion)
#
sub output_table {
    my( $self, $header_name, $results, $key ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='ManageResources.$header_name' class='sortable'>
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
