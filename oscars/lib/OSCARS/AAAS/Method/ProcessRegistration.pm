#==============================================================================
package OSCARS::AAAS::Method::ProcessRegistration;

=head1 NAME

OSCARS::AAAS::Method::ProcessRegistration - process user registration.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ProcessRegistration;

=head1 DESCRIPTION

This class is sub-class of OSCARS::Method.  It is not currently
functional, but is intended for use by an administrator with Shibboleth and I2
BRUW.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang  (dapi@umich.edu)

=head1 LAST MODIFIED

January 9, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::User;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


###############################################################################
# soap_method:  Process a user's registration.  Not functional
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self, @insertions ) = @_;

    my $results = {};
    my $user_dn = $self->{user}->{dn};

    my $encrypted_password = $self->{params}->{password_once};

    # get current date/time string in GMT
    my $current_date_time = $self->{params}->{utc_seconds};
	
    # login name overlap check
    my $statement = 'SELECT user_dn FROM users WHERE user_dn = ?';
    my $row = $self->{user}->get_row($statement, $user_dn);

    if ( $row ) {
        throw Error::Simple('The selected login name is already taken ' .
                   'by someone else; please choose a different login name.');
    }

    $statement = 'INSERT INTO users VALUES ( ' .
                              '?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )';
    my $unused = $self->{user}->do_query($statement, @insertions);

    $results->{status_msg} = 'Your user registration has been recorded ' .
        "successfully. Your login name is <strong>$user_dn</strong>. Once " .
        'your registration is accepted, information on ' .
        'activating your account will be sent to your primary email address.';
    $self->{logger}->add_string($results->{status_msg});
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


######
1;
