#==============================================================================
package OSCARS::AAA::Method::Authorization::List;

=head1 NAME

OSCARS::AAA::Method::Authorization::List - Handles authorizations

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Authorization::List;

=head1 DESCRIPTION

This is an AAA SOAP method.  It manages the retrieval of information from
the permissions, resources, resourcepermissions and authorizations tables, as 
well as additions and deletions upon the authoriozations table,  The specific 
operation to perform is given by the 'op' parameter. 

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 19, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAA::ResourceLibrary;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{lib} = OSCARS::AAA::ResourceLibrary->new('db' => $self->{db});
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information for the Manage Authorizations page. 
#     It returns information from the resources, permissions, and
#     authorizations tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage authorizations");
    }
    my $results = {};
    my $statement = "SELECT login FROM users";
    $results->{users} = {};
    my $auxResults = $self->{db}->doQuery($statement);
    for my $row (@$auxResults) { $results->{users}->{$row->{login}} = 1; }

    $results->{resourcePermissions} =
        $self->{lib}->getResourcePermissions( $self->{params} );
    return $results;
} #____________________________________________________________________________


######
1;
