#==============================================================================
package OSCARS::Library::AAA::Common;

=head1 NAME

OSCARS::Library::AAA::Common - library for permissions, resources, auths

=head1 SYNOPSIS

  use OSCARS::Library::AAA::Common;

=head1 DESCRIPTION

Common library for operations on permissions, resources, resourcePermissions,
and authorizations tables.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $self into designated class.
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# addRow:  Add a row to the AAA resources, permissions, or
#           resourcePermissions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub addRow {
    my( $self, $params, $tableName ) = @_;

    my $results = {};
    my $statement = "SHOW COLUMNS from $tableName";
    my $rows = $self->{db}->doQuery( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    $statement = "INSERT INTO $tableName VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
             
    my $unused = $self->{db}->doQuery($statement, @insertions);
    return;
} #____________________________________________________________________________


sub getResourcePermissions {
    my( $self, $params ) = @_;

    my( $resourceName, $permissionName, $auxResult );
    my $statement = "SELECT resourceId, permissionId " .
                    "FROM resourcePermissions";
    my $resourcePermissions = {};
    my $rpResults = $self->{db}->doQuery($statement);
    $statement = "SELECT name FROM resources WHERE id = ?";
    my $pstatement = "SELECT name FROM permissions WHERE id = ?";
    for my $row (@$rpResults) {
        $auxResult = $self->{db}->getRow($statement, $row->{resourceId});
        $resourceName = $auxResult->{name};
        if ( !$resourcePermissions->{$resourceName} ) {
            $resourcePermissions->{$resourceName} = {};
        }
        $auxResult = $self->{db}->getRow($pstatement, $row->{permissionId});
        $permissionName = $auxResult->{name};
        $resourcePermissions->{$resourceName}->{$permissionName} = 1;
    }
    return $resourcePermissions;
} #____________________________________________________________________________


######
1;
