#==============================================================================
package OSCARS::AAAS::Method::SetProfile;

=head1 NAME

OSCARS::AAAS::Method::SetProfile - SOAP method to modify user profile fields.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::SetProfile;

=head1 DESCRIPTION

SOAP method for modifying the user profile contained in the users table.
It inherits from OSCARS::Method.

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
use OSCARS::AAAS::UserCommon;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};


sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{user_common} = OSCARS::AAAS::UserCommon->new();
    $self->{param_tests} = {
        'user_password' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's current password."
            }
        ),
        'user_last_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's last name."
            }
        ),
        'user_first_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's first name."
            }
        ),
        'institution' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's organization."
            }
        ),
        'user_email_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary email address."
            }
        ),
        'user_phone_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary phone number."
            }
        )
    };
} #____________________________________________________________________________


###############################################################################
# soap_method:  Modifies the user profile for a particular user.  If the
#     user has admin privileges, they can set the information for another
#     user.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    my( $institution_id );
    my $user_dn = $self->{user}->{dn};

    # Read the current user information from the database to decide which
    # fields are being updated, and user has proper privileges.

    # DB query: get the user profile detail
    my $statement = "SELECT $self->{user_profile_fields} FROM users where user_dn = ?";
    my $results = $self->{user}->get_row($statement, $user_dn);

    # check whether this person is in the database
    if ( !$results ) {
        throw Error::Simple("User $user_dn does not have an OSCARS login.");
    }

    ### Check the current password with the one in the database before
    ### proceeding.
    if ( $results->{user_password} ne
         crypt($self->{params}->{user_password}, 'oscars') ) {
        throw Error::Simple(
            'Please check the current password and try again.');
    }

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $self->{params}->{password_new_once} ) {
        $self->{params}->{user_password} = crypt( $self->{params}->{password_new_once},
                                         'oscars');
    }
    else {
        $self->{params}->{user_password} = crypt($self->{params}->{user_password}, 'oscars');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions.
    if ( $self->{params}->{institution} ) {
        $self->{params}->{institution_id} =
                $self->get_institution_id($self->{params}->{institution});
    }

    # prepare the query for database update
    $statement = 'UPDATE users SET ';
    my @fields = split(', ', $self->{user_profile_fields});
    for $_ (@fields) {
        $statement .= "$_ = '$self->{params}->{$_}', ";
        # TODO:  check that query preparation correct
        $results->{$_} = $self->{params}->{$_};
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE user_dn = ?';
    my $unused = $self->{user}->do_query($statement, $user_dn);

    $results->{institution} = $self->{params}->{institution};
    $results->{user_password} = undef;
    return $results;
} #____________________________________________________________________________


######
1;
