#==============================================================================
package OSCARS::AAAS::Method::GetProfile;

=head1 NAME

OSCARS::AAAS::Method::GetProfile - SOAP method to get an OSCARS user profile

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::GetProfile;

=head1 DESCRIPTION

This is an AAAS SOAP method.  It gets the user profile information from
the users table.  It inherits from OSCARS::Method.

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
use OSCARS::AAAS::UserCommon;

our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{user_common} = OSCARS::AAAS::UserCommon->new();
} #____________________________________________________________________________


###############################################################################
# soap_method:  Gets the user profile from the database.  If the user has
#     admin privileges, show all fields.  If the user is an admin, they can
#     request the profile of another user.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my( $statement, $results );

    my $user_dn = $self->{user}->{dn};
    if ( $self->{params}->{admin_permission} ) {
        $statement = 'SELECT * FROM users where user_dn = ?';
        # coming in from view users list
        if( $self->{params}->{id} ) {
            $results = $self->{user}->get_row($statement, $self->{params}->{id});
        }
        else {
            $results = $self->{user}->get_row($statement, $user_dn);
        }
    }
    else {
        $statement = "SELECT $self->{user_common}->{user_profile_fields} FROM users where user_dn = ?";
        $results = $self->{user}->get_row($statement, $user_dn);
    }

    # check whether this person is a registered user
    # (love that syntax:  testing for rows will not work because ref not
    #  empty)
    if ( !$results ) {
        throw Error::Simple("No such user $user_dn.");
    }
    if ( $self->{params}->{id} ) {
        $results->{id} = $self->{params}->{id};
    }

    $statement = 'SELECT institution_name FROM institutions
              WHERE institution_id = ?';
    my $irow = $self->{user}->get_row($statement,
                                        $results->{institution_id});

    # check whether this organization is in the db
    if ( !$irow ) {
        throw Error::Simple( 'Organization not found.' );
    }
    $results->{institution_id} = $irow->{institution_name};
    # X out password
    $results->{user_password} = undef;
    $self->{logger}->add_string("Successfully retrieved user profile");
    $self->{logger}->add_hash($results);
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


######
1;
