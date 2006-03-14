#==============================================================================
package OSCARS::AAAS::AuthN;

=head1 NAME

OSCARS::AAAS::AuthN - performs authenticatication for OSCARS

=head1 SYNOPSIS

  use OSCARS::AAAS::AuthN;

=head1 DESCRIPTION

Performs authentication required to access OSCARS.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

February 10, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);


sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    bless($self, $class);
    return($self);
} #____________________________________________________________________________


###############################################################################
# authenticate:  authenticates user
#
# In:  OSCARS::User instance, hash of paraeters
# Out: None
#
sub authenticate {
    my( $self, $user, $params ) = @_;

    if ($user->authenticated()) { return 1; }
    if (!$params->{user_password}) {
        throw Error::Simple('Attempting to access a SOAP method before authenticating.');
    }
    # Get the password and privilege level from the database.
    my $statement = 'SELECT user_password FROM users WHERE user_login = ?';
    my $results = $user->get_row($statement, $user->{login});
    # Make sure user exists.
    if ( !$results ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    # compare passwords
    my $encoded_password = crypt($params->{user_password}, 'oscars');
    if ( $results->{user_password} ne $encoded_password ) {
        # see if password already encrypted
        if ( $results->{user_password} ne $params->{user_password} ) {
            throw Error::Simple('Please check your password and try again.');
	}
    }
    $user->set_authenticated(1);
} #____________________________________________________________________________


######
1;
