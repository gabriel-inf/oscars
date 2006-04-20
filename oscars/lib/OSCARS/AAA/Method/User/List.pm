#==============================================================================
package OSCARS::AAA::Method::User::List;

=head1 NAME

OSCARS::AAA::Method::User::List - Retrieves list of system users.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::User::List;

=head1 DESCRIPTION

SOAP method to view information about all users.  It inherits from 
OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 19, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::AAA::Method::Institution::List;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::Institution::List->new();
    $self->{paramTests} = {};
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Retrieves a list of all system users.
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
    $results->{list} = $self->getUsers($self->{params});
    return $results;
} #____________________________________________________________________________


###############################################################################
# getUsers:  Retrieves all information from users table.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub getUsers {
    my( $self, $params ) = @_;

    my $statement = "SELECT * FROM users WHERE status != 'role' " .
                    "ORDER BY lastName";
    my $results = $self->{db}->doQuery($statement);
    for my $oscarsUser (@$results) {
        $oscarsUser->{institutionName} = $self->{institutions}->getName(
                        $self->{db}, $oscarsUser->{institutionId});
	$oscarsUser->{institutionId} = 'hidden';
        $oscarsUser->{password} = 'hidden';
	$oscarsUser->{id} = 'hidden';
    }
    return $results;
} #____________________________________________________________________________


######
1;
