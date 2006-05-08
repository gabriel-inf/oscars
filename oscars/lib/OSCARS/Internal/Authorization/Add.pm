#==============================================================================
package OSCARS::Internal::Authorization::Add;

=head1 NAME

OSCARS::Internal::Authorization::Add - Adds an authorization.

=head1 SYNOPSIS

  use OSCARS::Internal::Authorization::Add;

=head1 DESCRIPTION

This is an internal SOAP method.  It adds an authorization to the authorizations
table for a specific user.

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
    $self->{paramTests} = {
        'permissionName' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission name."
            }
        ),
        'resourceName' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource name."
            }
        ),
        'login' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
    }
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Add a new authorization, given user, permission, and 
#              resource names.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to add authorization");
    }
    my $response = {};
    $response->{login} = $request->{login};
    return $response;
} #____________________________________________________________________________


######
1;
