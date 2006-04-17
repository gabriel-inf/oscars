#==============================================================================
package OSCARS::AAA::ResourceLibrary;

=head1 NAME

OSCARS::AAA::ResourceLibrary - library for permissions, resources, auths

=head1 SYNOPSIS

  use OSCARS::AAA::ResourceLibrary;

=head1 DESCRIPTION

Common library for operations on permissions, resources, resourcepermissions,
and authorizations tables.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $_self into designated class.
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# add_row:  Add a row to the AAA resources, permissions, or
#           resourcepermissions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub add_row {
    my( $self, $params, $table_name ) = @_;

    my $results = {};
    my $statement = "SHOW COLUMNS from $table_name";
    my $rows = $self->{db}->do_query( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    $statement = "INSERT INTO $table_name VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
             
    my $unused = $self->{db}->do_query($statement, @insertions);
    return;
} #____________________________________________________________________________


sub get_resource_permissions {
    my( $self, $params ) = @_;

    my( $resource_name, $permission_name, $aux_result );
    my $statement = "SELECT resource_id, permission_id " .
                    "FROM resourcepermissions";
    my $resource_permissions = {};
    my $rp_results = $self->{db}->do_query($statement);
    $statement = "SELECT resource_name FROM resources WHERE resource_id = ?";
    my $pstatement = "SELECT permission_name FROM permissions " .
                     "WHERE permission_id = ?";
    for my $row (@$rp_results) {
        $aux_result = $self->{db}->get_row($statement, $row->{resource_id});
        $resource_name = $aux_result->{resource_name};
        if ( !$resource_permissions->{$resource_name} ) {
            $resource_permissions->{$resource_name} = {};
        }
        $aux_result = $self->{db}->get_row($pstatement, $row->{permission_id});
        $permission_name = $aux_result->{permission_name};
        $resource_permissions->{$resource_name}->{$permission_name} = 1;
    }
    return $resource_permissions;
} #____________________________________________________________________________


###############################################################################
# id_from_name:  Gets the id associated with a name.
#
# In:  string (resource, permission, or resourcepermission), and name value
# Out: integer id
#
sub id_from_name {
    my( $self, $selector, $field_value ) = @_;

    my $id_str = $selector . '_id';
    my $field_str = $selector . '_name';
    my $statement = "SELECT $id_str FROM $selector" . "s WHERE $field_str = ?";
    my $row = $self->{db}->get_row($statement, $field_value);
    return $row->{$id_str};
} #____________________________________________________________________________


######
1;
