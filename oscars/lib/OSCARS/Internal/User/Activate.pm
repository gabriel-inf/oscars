#==============================================================================
package OSCARS::Internal::User::Activate;

=head1 NAME

OSCARS::Internal::User::Activate - Currently a noop.

=head1 SYNOPSIS

  use OSCARS::Internal::User::Activate;

=head1 DESCRIPTION

SOAP method to activate a user.  Currently a noop.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 27, 2006

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
# soapMethod:  Gets all information necessary for the Manage Users page. 
#     It returns information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to activate user");
    }
    my $results = $self->activateAccount();
    my $msg = 'The user account <strong>' .
       "$self->{user}->{login}</strong> has been successfully activated. You " .
       'will be redirected to the main service login page in 10 seconds. ' .
       '<br>Please change the password to your own once you sign in.';
    return $results;
} #____________________________________________________________________________


###############################################################################
# activateAccount:  Activate a user's account.  Not functional.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub activateAccount {
    my( $self ) = @_;

    my $results = {};
    my $login = $self->{user}->{login};
    # get the password from the database
    my $statement = 'SELECT activationKey FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);
    if ( !$row ) {
        throw Error::Simple("User $login has not registered yet.");
    }

    if ( $row->{activationKey} eq '' ) {
        throw Error::Simple('This account has already been activated.');
    }
    elsif ( $row->{activationKey} ne $self->{params}->{activationKey} ) {
        throw Error::Simple('Please check the activation key and try again.');
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
