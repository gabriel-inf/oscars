#==============================================================================
package OSCARS::AAAS::Method::Login;

=head1 NAME

OSCARS::AAAS::Method::Login - SOAP method for OSCARS login.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::Login;

=head1 DESCRIPTION

SOAP method for OSCARS login.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 3, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

our @ISA = qw{OSCARS::Method};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{param_tests} = {
        # must be valid email address
        'user_login' => (
            {'regexp' => '.+',
             'error' => "Please enter your login name."
            }
        ),
        'user_password' => (
            {'regexp' => '.+',
             'error' => "Please enter your password."
            }
        )
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  Log in to OSCARS.  Authentication, if it was necessary, has
#               already been performed by the resource manager
#               querying OSCARS::AAAS::AuthN
# In:  reference to hash of parameters
# Out: reference to hash of results containing user login.
#
sub soap_method {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    my $user_login = $self->{user}->{login};
    my $results = {};
    $results->{user_login} = $user_login;
    $results->{user_password} = 'hidden';
    # used to indicate which tabbed pages that require authorization can
    # be displayed
    $results->{authorized} = {};
    if ( $self->{user}->authorized('Users', 'manage') ) {
        $results->{authorized}->{ManageUsers} = 1;
    }
    if ( $self->{user}->authorized('Domains', 'manage') ) {
        $results->{authorized}->{ManageDomains} = 1;
    }
    $self->{logger}->info("finish", $results);
    return $results;
} #____________________________________________________________________________


######
1;
