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

April 27, 2006

=cut


use strict;

use Error qw(:try);
use Data::Dumper;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
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

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to add user");
    }
    my $login = $self->{params}->{selectedUser};
    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);
    if ( $row ) {
        throw Error::Simple("The login, $login, is already " .
	       	"taken by someone else; please choose a different login name.");
    }
    my $fields = $self->buildFields($self->{params});
    my $statement = "INSERT INTO users VALUES(" .
                     join(', ', @$fields) . ")";
    $self->{db}->execStatement($statement);

    my $results = {};
    $statement = "SELECT * FROM UserList";
    $results->{list} = $self->{db}->doSelect($statement);
    $msg = "$self->{user}->{login} added user $login}";
    return $results;
} #____________________________________________________________________________


###############################################################################
# buildFields:  Build fields for db insertion, quoting fields where necessary.
#
sub buildFields {
    my( $self, $params ) = @_;

    my @fields = ();
    push( @fields, 'NULL' );
    push( @fields, "'$params->{selectedUser}'" );    # login
    push( @fields,
        $params->{certificate} ? "'$params->{certificate}'" : 'NULL' ); 
    push( @fields,
        $params->{certSubject} ? "'$params->{certSubject}'" : 'NULL' );  
    push( @fields, "'$params->{lastName}'" );
    push( @fields, "'$params->{firstName}'" ); 
    push( @fields, "'$params->{emailPrimary}'" );
    push( @fields, "'$params->{phonePrimary}'" );
    my $password = crypt($params->{passwordNewOnce}, 'oscars');
    push( @fields, "'$password'" );
    push( @fields,
        $params->{description} ? "'$params->{description}'" : 'NULL' ); 
    push( @fields,
        $params->{emailSecondary} ? "'$params->{emailSecondary}'" : 'NULL' ); 
    push( @fields,
        $params->{phoneSecondary} ? "'$params->{phoneSecondary}'" : 'NULL' ); 
    push( @fields, $params->{status} ? "'$params->{status}'" : 'NULL' ); 
    push( @fields,
        $params->{activationKey} ? "'$params->{activationKey}'" : 'NULL' ); 
    push( @fields,
        $params->{lastActiveTime} ? $params->{lastActiveTime} : 'NULL' ); 
    push( @fields,
        $params->{registerTime} ? $params->{registerTime} : 'NULL' ); 
    my $statement = 'SELECT id FROM institutions WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $params->{institutionName});
    push( @fields, $row->{id} );    # institutionId
    return \@fields;
} #____________________________________________________________________________


######
1;
