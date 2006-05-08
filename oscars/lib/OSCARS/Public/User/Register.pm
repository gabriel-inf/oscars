#==============================================================================
package OSCARS::Public::User::Register;

=head1 NAME

OSCARS::Public::User::Register - Handles user registration.

=head1 SYNOPSIS

  use OSCARS::Public::User::Register;

=head1 DESCRIPTION

SOAP method to handle user registration.  Currently a noop.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

May 4, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::Internal::User::Add;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Registers a user.  Currently a noop. 
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $response = {};
    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $request->{selectedUser});
    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }
    # TODO:  call OSCARS::Internal::User::Add
    my $msg = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$request->{selectedUser}</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    return $response;
} #____________________________________________________________________________


######
1;
