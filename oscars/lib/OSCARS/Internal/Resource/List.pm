#==============================================================================
package OSCARS::Internal::Resource::List;

=head1 NAME

OSCARS::Internal::Resource::List - SOAP method to list resources

=head1 SYNOPSIS

  use OSCARS::Internal::Resource::List;

=head1 DESCRIPTION

This is an internal SOAP method.  It returns information from the resources 
table.

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
# soapMethod:  Lists information in the resources table.
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
    my( $resourceName, $permissionName );

    my $response = {};
    my $statement = "SELECT name FROM resources";
    $response->{resources} = {};
    my $rresults = $self->{db}->doSelect($statement);
    for my $row (@$rresults) {
        $response->{resources}->{$row->{name}} = 1;
    }

    my $statement = "SELECT name FROM permissions";
    $response->{permissions} = {};
    my $presults = $self->{db}->doSelect($statement);
    for my $row (@$presults) {
        $response->{permissions}->{$row->{name}} = 1;
    }

    $response->{resourcePermissions} =
                       $self->{lib}->getResourcePermissions();
    return $response;
} #____________________________________________________________________________


######
1;
