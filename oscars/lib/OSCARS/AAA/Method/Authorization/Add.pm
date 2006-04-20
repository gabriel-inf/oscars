#==============================================================================
package OSCARS::AAA::Method::Authorization::Add;

=head1 NAME

OSCARS::AAA::Method::Authorization::Add - Adds an authorization.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Authorization::Add;

=head1 DESCRIPTION

This is an AAA SOAP method.  It adds an authorization to the authorizations
table for a specific user.

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
    $results->{login} = $self->{params}->{login};
    return $results;
} #____________________________________________________________________________


######
1;
