#==============================================================================
package OSCARS::Internal::User::Add;

=head1 NAME

OSCARS::Internal::User::Add - Adds a new reservation system user.

=head1 SYNOPSIS

  use OSCARS::Internal::User::Add;

=head1 DESCRIPTION

SOAP method to add a new system user.  It inherits from OSCARS::Method.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 20, 2006

=cut


use strict;

use Error qw(:try);

use OSCARS::Public::Institution::List;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{institutions} = OSCARS::Public::Institution::List->new();
    $self->{paramTests} = {};
    $self->{paramTests} = {
        'selectedUser' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's distinguished name."
            }
        ),
        'passwordNewOnce' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's current password."
            }
        ),
        'lastName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's last name."
            }
        ),
        'firstName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's first name."
            }
        ),
        'institutionName' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's organization."
            }
        ),
        'emailPrimary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary email address."
            }
        ),
        'phonePrimary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary phone number."
            }
        )
    };
} #____________________________________________________________________________


###############################################################################
# soapMethod:  Gets all information necessary for the Manage Users page. 
#     It returns information from the users and institutions tables.
#
# In:  reference to hash of parameters
# Out: reference to hash of results
#
sub soapMethod {
    my( $self ) = @_;

    my $msg;

    my $params = $self->{params};
    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage users");
    }
    my $results = {};
    $params->{login} = $params->{selectedUser};

    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $params->{login});
    if ( $row ) {
        throw Error::Simple("The login, $params->{login}, is already " .
	       	"taken by someone else; please choose a different login name.");
    }
    # Set the institution id to the primary key in the institutions
    # table (user only can select from menu of existing instituions).
    if ( $params->{institutionName} ) {
        $params->{institutionId} = $self->{institutions}->getId($self->{db},
                                                  $params->{institutionName});
    }
    $params->{password} = crypt($params->{passwordNewOnce}, 'oscars');
    $statement = 'SHOW COLUMNS from users';
    my $rows = $self->{db}->doQuery( $statement );

    my @insertions;
    # TODO:  FIX way to get insertions fields
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }
    $statement = "INSERT INTO users VALUES ( " .
             join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{db}->doQuery($statement, @insertions);
    $msg = "$self->{user}->{login} added user $self->{params}->{selectedUser}";
    return $results;
} #____________________________________________________________________________

######
1;
