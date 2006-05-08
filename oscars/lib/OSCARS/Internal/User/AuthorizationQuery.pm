#==============================================================================
package OSCARS::Internal::User::AuthorizationQuery;

=head1 NAME

OSCARS::Internal::User::AuthorizationQuery - Queries user authorizations.

=head1 SYNOPSIS

  use OSCARS::Internal::User::AuthorizationQuery;

=head1 DESCRIPTION

This is an internal SOAP method.  It returns information about all the
authorizations a particular user currently has.

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
# soapMethod:  Gets information about a particular user's authorizations.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage authorizations");
    }
    my $response = {};
    my $statement = "SELECT login FROM users";
    $response->{users} = {};
    my $auxResults = $self->{db}->doSelect($statement);
    for my $row (@$auxResults) { $response->{users}->{$row->{login}} = 1; }

    $response->{resourcePermissions} =
        $self->{lib}->getResourcePermissions( $request );
    return $response;
} #____________________________________________________________________________

######
1;
