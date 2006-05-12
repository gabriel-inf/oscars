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

May 11, 2006

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
# soapMethod:  Adds an OSCARS user by inserting information into the users
#     table.
#
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    if ( !$self->{user}->authorized('Users', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to add user");
    }
    my $login = $request->{selectedUser};
    # login name overlap check
    my $statement = 'SELECT login FROM users WHERE login = ?';
    my $row = $self->{db}->getRow($statement, $login);
    if ( $row ) {
        throw Error::Simple("The login, $login, is already " .
	       	"taken by someone else; please choose a different login name.");
    }
    my $fields = $self->buildFields( $request );
    my $statement = "INSERT INTO users VALUES(" .
                     join(', ', @$fields) . ")";
    $self->{db}->execStatement($statement);

    my $response = {};
    $response->{login} = $login;
    return $response;
} #____________________________________________________________________________


###############################################################################
# buildFields:  Build fields for db insertion, quoting fields where necessary.
#
sub buildFields {
    my( $self, $request ) = @_;

    my @fields = ();
    push( @fields, 'NULL' );
    push( @fields, "'$request->{selectedUser}'" );    # login
    push( @fields,
        $request->{certificate} ? "'$request->{certificate}'" : 'NULL' ); 
    push( @fields,
        $request->{certSubject} ? "'$request->{certSubject}'" : 'NULL' );  
    push( @fields, "'$request->{lastName}'" );
    push( @fields, "'$request->{firstName}'" ); 
    push( @fields, "'$request->{emailPrimary}'" );
    push( @fields, "'$request->{phonePrimary}'" );
    my $password = crypt($request->{passwordNewOnce}, 'oscars');
    push( @fields, "'$password'" );
    push( @fields,
        $request->{description} ? "'$request->{description}'" : 'NULL' ); 
    push( @fields,
        $request->{emailSecondary} ? "'$request->{emailSecondary}'" : 'NULL' ); 
    push( @fields,
        $request->{phoneSecondary} ? "'$request->{phoneSecondary}'" : 'NULL' ); 
    push( @fields, $request->{status} ? "'$request->{status}'" : 'NULL' ); 
    push( @fields,
        $request->{activationKey} ? "'$request->{activationKey}'" : 'NULL' ); 
    push( @fields,
        $request->{lastActiveTime} ? $request->{lastActiveTime} : 'NULL' ); 
    push( @fields,
        $request->{registerTime} ? $request->{registerTime} : 'NULL' ); 
    my $statement = 'SELECT id FROM institutions WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $request->{institutionName});
    push( @fields, $row->{id} );    # institutionId
    return \@fields;
} #____________________________________________________________________________


######
1;
