#==============================================================================
package OSCARS::AAA::Method::Permission::Add;

=head1 NAME

OSCARS::AAA::Method::Permission::Add - SOAP method to add a permission

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Permission::Add;

=head1 DESCRIPTION

This is an AAA SOAP method.  It adds a new permission in the permissions
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
    $self->{paramTests}->{PermissionAdd} = {
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
# soapMethod:  Gets all information necessary for the Manage Permissions page.
#     It returns information from the permissions table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage permissions");
    }
    $self->{lib}->addRow( $self->{params},'Permissions' );
    my $results = {};
    return $results;
} #____________________________________________________________________________


######
1;
