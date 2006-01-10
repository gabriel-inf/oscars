#==============================================================================
package OSCARS::AAAS::Method::DeleteUser;

=head1 NAME

OSCARS::AAAS::Method::DeleteUser - SOAP method to delete an OSCARS user account.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::DeleteUser;

=head1 DESCRIPTION

This class is an AAAS SOAP method to delete an OSCARS user account.
It inherits from OSCARS::Method

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

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
# soap_method:  Deletes user with the given distinguished name
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $statement = 'DELETE from users where user_dn = ?';
    my $unused = $self->{user}->do_query($statement, $self->{params}->{id});
    $self->{logger}->add_string("Deleted user with id $self->{params}->{id}");
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $self->{params};
} #____________________________________________________________________________


######
1;
