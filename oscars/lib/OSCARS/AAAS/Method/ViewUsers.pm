#==============================================================================
package OSCARS::AAAS::Method::ViewUsers;

=head1 NAME

OSCARS::AAAS::Method::ViewUsers - SOAP method to view OSCARS users' information.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::ViewUsers;

=head1 DESCRIPTION

SOAP method to view information about all OSCARS users.  It is only callable
by users with administrative privileges.  It inherits from OSCARS::Method.

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


###############################################################################
# soap_method:  Retrieves the profile information for all users.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my $irow;

    my $statement .= 'SELECT * FROM users ORDER BY user_last_name';
    my $results = $self->{user}->do_query($statement);

    for my $user (@$results) {
        # replace institution id with institution name
        $statement = 'SELECT institution_name FROM institutions ' .
                 'WHERE institution_id = ?';
        $irow = $self->{user}->get_row($statement, $user->{institution_id});
        $user->{institution_id} = $irow->{institution_name};
        $user->{user_password} = undef;
    }
    $self->{logger}->add_string("Successfully viewed user list");
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


######
1;
