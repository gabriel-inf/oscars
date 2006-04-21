#==============================================================================
package OSCARS::Internal::Resource::Add;

=head1 NAME

OSCARS::Internal::Resource::Add - SOAP method to add a resource.

=head1 SYNOPSIS

  use OSCARS::Internal::Resource::Add;

=head1 DESCRIPTION

This is an internal SOAP method.  It adds a new row to the resources table.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

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
            'error' => "Please enter the resource's name."
            }
        ),
        'description' => (
            {'regexp' => '.+',
            'error' => "Please enter the resource's description."
            }
        ),
    };
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
    $self->{lib}->addRow( $self->{params}, 'Resources' );
    return $results;
} #____________________________________________________________________________


######
1;
