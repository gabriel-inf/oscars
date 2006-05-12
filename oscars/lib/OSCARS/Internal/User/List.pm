#==============================================================================
package OSCARS::Internal::User::List;

=head1 NAME

OSCARS::Internal::User::List - Retrieves list of system users.

=head1 SYNOPSIS

  use OSCARS::Internal::User::List;

=head1 DESCRIPTION

SOAP method to view information about all users.  It inherits from 
OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 11, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Retrieves a list of all system users.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage users");
    }
    my $response = {};
    my $statement = "SELECT * FROM UserList";
    $response = $self->{db}->doSelect($statement);
    return $response;
} #____________________________________________________________________________


######
1;
