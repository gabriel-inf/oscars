#==============================================================================
package OSCARS::Internal::Resource::Remove;

=head1 NAME

OSCARS::Internal::Resource::Remove - SOAP method to remove a resource.

=head1 SYNOPSIS

  use OSCARS::Internal::Resource::Remove;

=head1 DESCRIPTION

This is an internal SOAP method.  It removes a resource from the resources 
table, and any corresponding resource/permission pairs in the 
resourcePermissions table.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 4, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Library::AAA::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{lib} = OSCARS::Library::AAA::Common->new('db' => $self->{db});
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Removes a resource. 
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage resources");
    }
    my $response = {};
    $self->removeResource( $request );
    return $response;
} #____________________________________________________________________________


###############################################################################
# removeResource:  Removes resource with the given name.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub removeResource {
    my( $self, $request ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $request->{resourceName});
    my $statement = 'DELETE FROM resources WHERE id = ?';
    $self->{db}->execStatement($statement, $resourceId);
    $self->removeResourcePermission($request);
} #____________________________________________________________________________


###############################################################################
# removeResourcePermission:  Removes a resource/permission pair.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub removeResourcePermission {
    my( $self, $request ) = @_;

    my $resourceId = $self->{lib}->idFromName('resource',
                                             $request->{resourceName});
    my $statement = 'DELETE FROM resourcePermissions WHERE resourceId = ?';
    $self->{db}->execStatement($statement, $resourceId);
} #____________________________________________________________________________


######
1;
