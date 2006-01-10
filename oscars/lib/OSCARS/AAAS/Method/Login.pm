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
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

January 9, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;

our @ISA = qw{OSCARS::Method};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{param_tests} = {
        # must be valid email address
        'user_dn' => (
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
# soap_method:  Log in to OSCARS.
#
# In:  reference to hash of parameters
# Out: reference to hash of results containing user dn and user level.
#
sub soap_method {
    my( $self ) = @_;

    my $user_dn = $self->{user}->{dn};

    # Get the password and privilege level from the database.
    my $statement = 'SELECT user_password, user_level FROM users WHERE user_dn = ?';
    my $results = $self->{user}->get_row($statement, $user_dn);
    # Make sure user exists.
    if ( !$results ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    # compare passwords
    my $encoded_password = crypt($self->{params}->{user_password}, 'oscars');
    if ( $results->{user_password} ne $encoded_password ) {
        throw Error::Simple('Please check your password and try again.');
    }
    $results->{user_dn} = $user_dn;
    # X out password
    $results->{user_password} = undef;
    $self->{logger}->add_string("User $user_dn successfully logged in");
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


######
1;
