#==============================================================================
package OSCARS::WBUI::AAA::ManageResources;

=head1 NAME

OSCARS::WBUI::AAA::ManageResources - Manage all resources.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAA::ManageResources;

=head1 DESCRIPTION

Manage all OSCARS resources, including the handling of modification,
delete, and addition of resources and permissions.  Requires admin privileges.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  print resources form, with results retrieved via SOAP call
#
sub output_div {
    my( $self, $results, $authorizations ) = @_;

    my $msg = "OSCARS resources";
    print( qq{
    <div>
    <p>To add a resource, fill in the required fields, and then click on
    'Add'.  To delete a resource, click on that row in the resources table,
    and click 'Delete'.  This form also associates permissions with a resource.
    Click on a resource and permission, and then 'Add', to add a permission 
    required to use a resource.</p>
    <form method='post' action=''>
    <table width='90%' class='auth-ui'>
    <tr>
    } );
    $self->entry_fields();
    $self->component_table('Resources', $results->{resources}, 'resource_name');
    $self->component_table('Permissions', $results->{permissions},
                        'permission_name');
    $self->ops_table();
    $self->resource_permissions_table($results->{resource_permissions});
    print("</tr></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# entry_fields:  print nested tables for entering resource and permissions
#     information.
#
sub entry_fields {
    my( $self ) = @_;

    print( qq{
    <td class='auth-ui-td'>
    <table class='auth-ui-td'>
    <tbody>
    <tr>
    } );
    $self->resource_entry_fields();
    print("</tr><tr>");
    $self->permission_entry_fields();
    print("</tr></tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# resource_entry_fields:  print fields for adding new resource information.
#
sub resource_entry_fields {
    my( $self ) = @_;

    my $add_submit_str = "return submit_form(this,
                    'component=AAA;method=ManageResources;op=addResource;');";
    my $delete_submit_str = "return submit_form(this,
                    'component=AAA;method=ManageResources;op=deleteResource;');";
    print( qq{
    <td class='auth-ui-td'>
    <table>
    <tbody>
    <tr>
      <td>Resource Name</td>
      <td><input class='required' type='text' name='resource_name'
           value=''></input>
      </td>
      <td><input type='button' onclick='return tse_addResource(this);' 
           value='Add'></input>
      </td>
    </tr>
    <tr>
      <td>Resource Description</td>
      <td><input type='text' name='resource_description'></input>
      </td>
      <td><input type='button' onclick='return tse_deleteResource(this);' 
           value='Delete'></input>
      </td>
    </tr>
    </tbody>
    </table>
    </td>
    } );
} #____________________________________________________________________________


###############################################################################
# permission_entry_fields:  print fields for adding new permission information.
#
sub permission_entry_fields {
    my( $self ) = @_;

    my $add_submit_str = "return submit_form(this,
                    'component=AAA;method=ManagePermissions;op=addPermission;');";
    my $delete_submit_str = "return submit_form(this,
                    'component=AAA;method=ManagePermissions;op=deletePermission;');";
    print( qq{
    <td class='auth-ui-td'>
    <table>
    <tbody>
    <tr>
      <td>Permission Name</td>
      <td><input class='required' type='text' name='permission_name'
           value=''></input>
      </td>
      <td><input type='button' onclick='return tse_addPermission(this);' 
           value='Add'></input>
      </td>
    </tr>
    <tr>
      <td>Permission Description</td>
      <td><input type='text' name='permission_description'></input>
      </td>
      <td><input type='button' onclick='return tse_deletePermission(this);' 
           value='Delete'></input>
      </td>
    </tr>
    </tbody>
    </table>
    </td>
    } );
} #____________________________________________________________________________


###############################################################################
# component_table:  output table for one component of resources (deletion)
#
sub component_table {
    my( $self, $header_name, $results, $key ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='ManageResources.$header_name' class='sortable'>
        <thead><tr><td>$header_name</td></tr></thead>
        <tbody>
    } );
    for my $name (sort keys %{$results}) {
        print("<tr><td>$name</td></tr>\n");
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# ops_table:  output table listing operations to be performed on
#     resources (add, delete)
#
sub ops_table {
    my( $self ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table class='auth-ui'>
      <tr><td><input type='button'
                  onclick='return tse_addResource(this);' 
                  value='Add -&gt;'></input></td></tr>
           <tr><td><input type='button' value='Delete &lt;-'></input></td></tr>
        </table>
      </td>
    } );
} #____________________________________________________________________________


###############################################################################
# resource_permissions_table:  output resource permissions table
#
sub resource_permissions_table {
    my( $self, $results ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Resources.ResourcePermissions' class='sortable'>
        <thead><tr><td>Resource</td><td>Requires</td></tr></thead>
        <tbody>
    } );
    for my $rkey (sort keys %{$results}) {
        for my $pkey (sort keys %{$results->{$rkey}}) {
            print("<tr><td>$rkey</td><td>$pkey</td></tr>");
        }
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


######
1;
