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

July 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'ResourceList';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  print resources form, with response retrieved via SOAP call
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    my $msg = "OSCARS resources";
    print( qq{
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
    $self->componentTable('Resources', $response->{resources}, 'resourceName');
    $self->componentTable('Permissions', $response->{permissions},
                        'permissionName');
    $self->opsTable();
    $self->resourcePermissionsTable($response->{resourcePermissions});
    print("</tr></table></form>\n");
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

    my $addSubmitStr = "return submitForm(this, 'method=ResourceAdd;');";
    my $deleteSubmitStr = "return submitForm(this,
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

    my $addSubmitStr = "return submitForm(this, 'method=PermissionAdd;');";
    my $deleteSubmitStr = "return submitForm(this, 'method=PermissionRemove;";
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
    my( $self, $headerName, $response, $key ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='ManageResources.$headerName' class='sortable'>
        <thead><tr><td>$headerName</td></tr></thead>
        <tbody>
    } );
    for my $name (sort keys %{$response}) {
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
    my( $self, $response ) = @_;

    print( qq{
      <td class='auth-ui-td'>
      <table id='Resources.ResourcePermissions' class='sortable'>
        <thead><tr><td>Resource</td><td>Requires</td></tr></thead>
        <tbody>
    } );
    for my $rkey (sort keys %{$response}) {
        for my $pkey (sort keys %{$response->{$rkey}}) {
            print("<tr><td>$rkey</td><td>$pkey</td></tr>");
        }
    }
    print("</tbody></table></td>\n");
} #____________________________________________________________________________


######
1;
