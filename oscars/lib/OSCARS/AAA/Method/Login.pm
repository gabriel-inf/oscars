#==============================================================================
package OSCARS::AAA::Method::Login;

=head1 NAME

OSCARS::AAA::Method::Login - SOAP method for login.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::Login;

=head1 DESCRIPTION

SOAP method for login.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

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
#               already been performed by the resource manager
#               querying OSCARS::AAA::AuthN
# In:  reference to hash of parameters
# Out: reference to hash of results containing user login.
#
sub soapMethod {
    my( $self ) = @_;

    $self->{logger}->info("start", $self->{params});
    my $login = $self->{user}->{login};
    my $results = {};
    $results->{login} = $login;
    $results->{password} = 'hidden';
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
