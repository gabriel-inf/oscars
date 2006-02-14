#==============================================================================
package OSCARS::WBUI::AAAS::ManagePermissions;

=head1 NAME

OSCARS::WBUI::AAAS::ManagePermissions - Manage all permissions.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::ManagePermissions;

=head1 DESCRIPTION

Manage OSCARS permissions, including modification, deletion, and addition.

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
# output:  print permissions form, with results retrieved via SOAP call
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header( -type=>'text/xml');
    print "<xml>\n";
    print qq{ <msg>OSCARS permissions</msg> };
    $self->{tabs}->output('ManagePermissions', $results->{authorizations});
    print qq{
    <div>
    <p>Click on 'Add' to add a permission.</p>
    <form method='post' action=''>
    <table width='90%' class='auth-ui'>
    <tr>
    };
    $self->output_permission_entry();
    $self->output_permissions($results->{permissions});
    print qq{ </tr></table></form></div> };
    print "</xml>\n";
} #____________________________________________________________________________


###############################################################################
# output_permission_entry: print form to enter new permission information
#
sub output_permission_entry {
    my( $self ) = @_;

    my $add_submit_str = "return submit_form(this,
                 'server=AAAS;method=ManagePermissions;op=addPermission;');";
    my $delete_submit_str = "return submit_form(this,
                 'server=AAAS;method=ManagePermissions;op=deletePermission;');";
    print qq{
    <td class='auth-ui-td'>
    <form method='post' action=''>
    <table class='sortable'>
    <tbody>
    <tr>
      <td>Permission Name</td>
      <td>
        <input class='required' type='text' name='permission_name' size='40'>
        </input>
       </td>
    </tr>
    <tr>
      <td>Permission Description</td>
      <td>
        <textarea name='permission_description' rows='3' cols='50'> </textarea>
      </td>
    </tr>
    </tbody>
    </table>
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


##############################################################################
# output_permissions:  output permissions table
#
sub output_permissions {
    my( $self, $results ) = @_;

    print qq{
      <td class='auth-ui-td'>
      <table id='Permissions.Permissions' class='sortable'>
        <thead><tr><td>Permissions</td></tr></thead>
        <tbody>
    };
    for my $name (sort keys %{$results}) {
        print qq{ <tr><td>$name</td></tr> };
    }
    print qq{ </tbody></table></td> };
} #____________________________________________________________________________


######
1;
