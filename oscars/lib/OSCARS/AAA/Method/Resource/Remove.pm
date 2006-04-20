#==============================================================================
package OSCARS::AAA::Method::Resource::Remove;

=head1 NAME

OSCARS::AAA::Method::Resource::Remove - SOAP method to remove a resource.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Resource::Remove;

=head1 DESCRIPTION

This is an AAA SOAP method.  It removes a resource from the resources table,
and any corresponding resource/permission pairs in the resourcePermissions
table.

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
# soapMethod:  Gets all information necessary for the Manage Resources page. 
#     It returns information from the resources and permissions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage resources");
    }
    my $results = {};
    $self->removeResource($self->{params} );
    return $results;
} #____________________________________________________________________________


###############################################################################
# removeResource:  Removes resource with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub removeResource {
    my( $self ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $self->{params}->{resourceName});
    my $statement = 'DELETE FROM resources WHERE id = ?';
    my $unused = $self->{db}->doQuery($statement, $resourceId);
    my $msg = "Removed resource with name $self->{params}->{resourceName}";
    $self->removeResourcePermission();
    return $msg;
} #____________________________________________________________________________


###############################################################################
# removeResourcePermission:  Removes a resource/permission pair.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub removeResourcePermission {
    my( $self ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $self->{params}->{resourceName});
    my $statement = 'DELETE FROM resourcePermissions WHERE resourceId = ?';
    my $unused = $self->{db}->doQuery($statement, $resourceId);
    my $msg = "Removed resource permission pair";
    return $msg;
} #____________________________________________________________________________


######
1;
