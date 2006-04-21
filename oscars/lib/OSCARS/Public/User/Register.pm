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

April 20, 2006

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
# soapMethod:  Registers a user.  Currently a noop. 
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my( $msg );

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage users");
    }
    my $results = {};
    my @insertions;
    my $params = $self->{params};
    my $login = $self->{user}->{login};

    my $encryptedPassword = $params->{passwordNewOnce};

    # get current date/time string in GMT
    my $currentDateTime = $params->{utcSeconds};
    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);
    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }
    $statement = 'INSERT INTO users VALUES ( ' .
                              '?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )';
    my $unused = $self->{db}->doQuery($statement, @insertions);

    $results->{msg} = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$login</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    my $msg = $results->{msg};
    return( $msg, $results );
} #____________________________________________________________________________


######
1;
