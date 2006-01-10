#==============================================================================
package OSCARS::AAAS::Method::AddUser;

=head1 NAME

OSCARS::AAAS::Method::AddUser - SOAP method to add an OSCARS user account.

=head1 SYNOPSIS

  use OSCARS::AAAS::Method::AddUser;

=head1 DESCRIPTION

This class is an AAAS SOAP Method to create an OSCARS user account.  It
inherits from OSCARS::Method.

=head1 BUGS

The user gets the administator's access privileges, rather than the
privileges set.  A priority to fix.

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
# soap_method:  Add a user to the OSCARS database.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soap_method {
    my ( $self ) = @_;

    my $results = {};
    my $user_dn = $self->{user}->{dn};

    my $encrypted_password = $self->{params}->{password_once};

    # login name overlap check
    my $statement = 'SELECT user_dn FROM users WHERE user_dn = ?';
    my $row = $self->{user}->get_row($statement, $user_dn);

    if ( $row ) {
        throw Error::Simple("The login, $user_dn, is already taken " .
                   'by someone else; please choose a different login name.');
    }

    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $self->{params}->{institution} ) {
        $self->{params}->{institution_id} =
           $self->get_institution_id($self->{params}->{institution});
    }

    $self->{params}->{user_password} = crypt($self->{params}->{password_new_once}, 'oscars');
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{user}->do_query( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($self->{params}->{$_->{Field}}) {
           $results->{$_->{Field}} = $self->{params}->{$_->{Field}};
           push(@insertions, $self->{params}->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    $statement = "INSERT INTO users VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
             
    my $unused = $self->{user}->do_query($statement, @insertions);
    # X out password
    $results->{id} = $user_dn;
    $results->{user_password} = undef;
    $self->{logger}->add_string("Successfully added OSCARS user $user_dn");
    $self->{logger}->add_hash($results);
    $self->{logger}->write_file($self->{user}->{dn}, $self->{params}->{method});
    return $results;
} #____________________________________________________________________________


######
1;
