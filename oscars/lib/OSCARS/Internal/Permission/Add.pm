#==============================================================================
package OSCARS::Internal::Permission::Add;

=head1 NAME

OSCARS::Internal::Permission::Add - SOAP method to add a permission

=head1 SYNOPSIS

  use OSCARS::Internal::Permission::Add;

=head1 DESCRIPTION

This is an internal SOAP method.  It adds a new permission in the permissions
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
    $self->{paramTests} = {
        'name' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's name."
            }
        ),
        'description' => (
            {'regexp' => '.+',
            'error' => "Please enter the permission's description."
            }
        ),
    };
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Handles adding a permission to the permissions table.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage permissions");
    }
    $self->{lib}->addRow( $request,'Permissions' );
    my $response = {};
    return $response;
} #____________________________________________________________________________


######
1;
