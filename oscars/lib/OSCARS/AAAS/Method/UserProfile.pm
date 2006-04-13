#==============================================================================
package OSCARS::AAA::Method::UserProfile;

=head1 NAME

OSCARS::AAA::Method::UserProfile - Handles unprivileged user's profile.

=head1 SYNOPSIS

  use OSCARS::AAA::Method::UserProfile;

=head1 DESCRIPTION

This is an AAA SOAP method.  It gets user profile information from
the users table.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)
Soo-yeon Hwang (dapi@umich.edu)

=head1 LAST MODIFIED

April 12, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAA::Method::ManageInstitutions;

our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::AAA::Method::ManageInstitutions->new();
    $self->{user_profile_fields} =
         'user_last_name, user_first_name, user_login, user_password, ' .
         'user_email_primary, user_email_secondary, ' .
         'user_phone_primary, user_phone_secondary, user_description, ' .
#    'user_register_time, user_activation_key, ' .
         'institution_id';
} #____________________________________________________________________________


###############################################################################
# soap_method:  SOAP method performing requested operation on a user's profile.
#     The default operation is to get the user's profile.  This method accesses
#     the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my( $self ) = @_;

    if ( !$self->{params}->{op} ) {
        throw Error::Simple(
            "Method $self->{params}->{method} requires specification of an operation");
    }
    my $results = {};
    if ($self->{params}->{op} eq 'viewProfile') {
        $results = $self->get_profile( $self->{user}, $self->{params} ); }
    elsif ($self->{params}->{op} eq 'modifyProfile') {
        $results = $self->modify_profile( $self->{user}, $self->{params} );
    }
    $results->{institution_list} =
                $self->{institutions}->get_institutions( $self->{user} );
    return $results;
} #____________________________________________________________________________


###############################################################################
# get_profile:  Gets the user profile for a particular user.  If the
#     user is coming in from the ManageProfile form, she can get more detailed 
#     information for herself or another user.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub get_profile {
    my( $self, $user, $params ) = @_;

    my( $statement, $results );

    # only happens if coming in from ManageProfile form, which requires
    # additional authorization
    if ( $params->{selected_user} ) {
        $statement = 'SELECT * FROM users WHERE user_login = ?';
        $results = $user->get_row($statement, $params->{selected_user});
        # check whether this person is in the database
        if ( !$results ) {
            throw Error::Simple("No such user $params->{selected_user}.");
        }
        $results->{user_id} = 'hidden';
    }
    else {
        $statement = "SELECT $self->{user_profile_fields} FROM users " .
                     'WHERE user_login = ?';
        $results = $user->get_row($statement, $user->{login});
    }
    $results->{institution_name} = $self->{institutions}->get_name(
                                           $user, $results->{institution_id});
    $results->{institution_id} = 'hidden';
    $results->{user_login} = $user->{login};
    $results->{selected_user} = $params->{selected_user};
    # X out password
    $results->{user_password} = 'hidden';
    return $results;
} #____________________________________________________________________________


###############################################################################
# modify_profile:  Modifies the user profile for a particular user.  If the
#     user is coming in via the ManageProfile form, she can set the 
#     information for another user.
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub modify_profile {
    my( $self, $user, $params ) = @_;

    my( $statement, $results );

    # only happens if coming in from ManageProfile form, which requires
    # additional authorization
    if ( $params->{selected_user} ) {
        # check whether this person is in the database
        $statement = 'SELECT user_login FROM users WHERE user_login = ?';
        $results = $user->get_row($statement, $params->{selected_user});
        if ( !$results ) {
            throw Error::Simple("No such user $params->{selected_user}.");
        }
    }
    $params->{user_login} = $user->{login};

    # If the password needs to be updated, set the input password field to
    # the new one.
    if ( $params->{password_new_once} ) {
        $params->{user_password} = crypt( $params->{password_new_once},
                                         'oscars');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions.
    if ( $params->{institution_name} ) {
        $params->{institution_id} = $self->{institutions}->get_id( 
                                          $user, $params->{institution_name} );
    }
    $results = {};    # clear any previous results
    # TODO:  allow admin to set all fields
    my @fields = split(', ', $self->{user_profile_fields});
    $statement = 'UPDATE users SET ';
    for $_ (@fields) {
        # TODO:  allow setting field to NULL where legal
        if ( $params->{$_} ) {
            $statement .= "$_ = '$params->{$_}', ";
            # TODO:  check that query preparation correct
            $results->{$_} = $params->{$_};
	}
    }
    $statement =~ s/,\s$//;
    $statement .= ' WHERE user_login = ?';
    my $unused = $user->do_query($statement, $params->{user_login});

    $results->{selected_user} = $params->{selected_user};
    $results->{user_login} = $user->{login};
    $results->{institution_name} = $params->{institution_name};
    $results->{user_password} = 'hidden';
    return $results;
} #____________________________________________________________________________


######
1;
