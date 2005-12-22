###############################################################################
package OSCARS::AAAS::Method::ActivateAccount;

=head1 NAME

OSCARS::AAAS::Method::ActivateAccount - activate user accoun.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ActivateAccount;

=head1 DESCRIPTION

This class inherits from OSCARS::Method.  It is not currently
functional, but is intended for use with Shibboleth and I2 BRUW.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

December 21, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soap_method:  Activate a user's account.  Not functional.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self, $required_level ) = @_;

    my ( $results) ;

    my $user_dn = $self->{user}->{dn};
    # get the password from the database
    my $statement = 'SELECT user_password, user_activation_key, user_level
                 FROM users WHERE user_dn = ?';
    my $row = $self->{user}->get_row($statement, $user_dn);

    # check whether this person is a registered user
    if ( !$row ) {
        throw Error::Simple('Please check your login name and try again.');
    }

    my $keys_match = 0;
    my( $pending_level, $non_match_error );
        # this login name is in the database; compare passwords
    if ( $row->{user_activation_key} eq '' ) {
        $non_match_error = 'This account has already been activated.';
    }
    elsif ( $row->{user_password} ne $self->{params}->{user_password} ) {
        $non_match_error = 'Please check your password and try again.';
    }
    elsif ( $row->{user_activation_key} ne 
            $self->{params}->{user_activation_key} ) {
        $non_match_error = 'Please check the activation key and ' .
                           'try again.';
    }
    else {
        $keys_match = 1;
        $pending_level = $row->{user_level};
    }

    # If the input password and the activation key matched against those
    # in the database, activate the account.
    if ( $keys_match ) {
        # Change the level to the pending level value and the pending level
        # to 0; empty the activation key field
        $statement = "UPDATE users SET user_level = ?, pending_level = ?,
                  user_activation_key = '' WHERE user_dn = ?";
        my $unused = $self->{user}->do_query($statement, $pending_level,
                                             $user_dn);
    }
    else {
        throw Error::Simple($non_match_error);
    }
    $results->{status_msg} = 'The user account <strong>' .
       "$user_dn</strong> has been successfully activated. You " .
       'will be redirected to the main service login page in 10 seconds. ' .
       '<br>Please change the password to your own once you sign in.';
    return $results;
} #____________________________________________________________________________ 

######
1;
