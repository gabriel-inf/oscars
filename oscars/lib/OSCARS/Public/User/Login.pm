#==============================================================================
package OSCARS::Public::User::Login;

=head1 NAME

OSCARS::Public::User::Login - SOAP method for login.

=head1 SYNOPSIS

  use OSCARS::Public::User::Login;

=head1 DESCRIPTION

SOAP method for login.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 4, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{paramTests} = {
        # must be valid email address
        'login' => (
            {'regexp' => '.+',
             'error' => "Please enter your login name."
            }
        ),
        'password' => (
            {'regexp' => '.+',
             'error' => "Please enter your password."
            }
        )
    };
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Log in.  Authentication, if it was necessary, has
#              already been performed.
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    $logger->info("start", $request);
    my $login = $self->{user}->{login};
    my $response = {};
    $response->{login} = $login;
    # used to indicate which tabbed pages that require authorization can
    # be displayed
    $response->{authorized} = {};
    if ( $self->{user}->authorized('Users', 'manage') ) {
        $response->{authorized}->{ManageUsers} = 1;
    }
    if ( $self->{user}->authorized('Domains', 'manage') ) {
        $response->{authorized}->{ManageDomains} = 1;
    }
    $logger->info("finish", $response);
    return $response;
} #____________________________________________________________________________


######
1;
