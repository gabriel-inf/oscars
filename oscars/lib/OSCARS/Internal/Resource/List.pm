#==============================================================================
package OSCARS::Internal::Resource::List;

=head1 NAME

OSCARS::Internal::Resource::List - SOAP method to list resources

=head1 SYNOPSIS

  use OSCARS::Internal::Resource::List;

=head1 DESCRIPTION

This is an internal SOAP method.  It returns information from the resources table.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 19, 2006

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
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage resources");
    }
    my( $resourceName, $permissionName );

    my $results = {};
    my $statement = "SELECT name FROM resources";
    $results->{resources} = {};
    my $rresults = $self->{db}->doQuery($statement);
    for my $row (@$rresults) {
        $results->{resources}->{$row->{name}} = 1;
    }

    my $statement = "SELECT name FROM permissions";
    $results->{permissions} = {};
    my $presults = $self->{db}->doQuery($statement);
    for my $row (@$presults) {
        $results->{permissions}->{$row->{name}} = 1;
    }

    $results->{resourcePermissions} =
                       $self->{lib}->getResourcePermissions();
    return $results;
} #____________________________________________________________________________


######
1;
