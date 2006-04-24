#==============================================================================
package OSCARS::WBUI::Method::ResourceList;

=head1 NAME

OSCARS::WBUI::Method::ResourceList - List all resources and permissions.

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ResourceList;

=head1 DESCRIPTION

Lists all resources and resource/permission pairs.  Requires proper 
authorization to use.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 22, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# outputDiv:  print resources form, with results retrieved via SOAP call
#
sub outputDiv {
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
    $self->entryFields();
    $self->componentTable('Resources', $results->{resources}, 'resourceName');
    $self->componentTable('Permissions', $results->{permissions},
                        'permissionName');
    $self->opsTable();
    $self->resourcePermissionsTable($results->{resourcePermissions});
    print("</tr></table></form></div>\n");
    return $msg;
} #____________________________________________________________________________


###############################################################################
# entryFields:  print nested tables for entering resource and permissions
#     information.
#
sub entryFields {
    my( $self ) = @_;

    print( qq{
    <td class='auth-ui-td'>
    <table class='auth-ui-td'>
    <tbody>
    <tr>
    } );
    $self->resourceEntryFields();
    print("</tr><tr>");
    $self->permissionEntryFields();
    print("</tr></tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# resourceEntryFields:  print fields for adding new resource information.
#
sub resourceEntryFields {
    my( $self ) = @_;

    my $addSubmitStr = "return submit_form(this, 'method=ResourceAdd;');";
    my $deleteSubmitStr = "return submit_form(this,
                    'method=ResourceRemove;');";
    print( qq{
    <td class='auth-ui-td'>
    <table>
    <tbody>
    <tr>
      <td>Resource Name</td>
      <td><input class='required' type='text' name='name'
           value=''></input>
      </td>
      <td><input type='button' onclick='return tse_addResource(this);' 
           value='Add'></input>
      </td>
    </tr>
    <tr>
      <td>Resource Description</td>
      <td><input class='SOAP' type='text' name='description'></input>
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
# permissionEntryFields:  print fields for adding new permission information.
#
sub permissionEntryFields {
    my( $self ) = @_;

    my $addSubmitStr = "return submit_form(this, 'method=PermissionAdd;');";
    my $deleteSubmitStr = "return submit_form(this, 'method=PermissionRemove;";
    print( qq{
    <td class='auth-ui-td'>
    <table>
    <tbody>
    <tr>
      <td>Permission Name</td>
      <td><input class='required' type='text' name='name'
           value=''></input>
      </td>
      <td><input type='button' onclick='return tse_addPermission(this);' 
           value='Add'></input>
      </td>
    </tr>
    <tr>
      <td>Permission Description</td>
      <td><input class='SOAP' type='text' name='description'></input>
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
# componentTable:  output table for one component of resources (deletion)
#
sub componentTable {
    my( $self, $headerName, $results, $key ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='ManageResources.$headerName' class='sortable'>
        <thead><tr><td>$headerName</td></tr></thead>
        <tbody>
    } );
    for my $name (sort keys %{$results}) {
        print("<tr><td>$name</td></tr>\n");
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


###############################################################################
# opsTable:  output table listing operations to be performed on
#     resources (add, delete)
#
sub opsTable {
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
# resourcePermissionsTable:  output resource permissions table
#
sub resourcePermissionsTable {
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
