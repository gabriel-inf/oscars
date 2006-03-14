# =============================================================================
package OSCARS::ResourceManager;

=head1 NAME

OSCARS::ResourceManager - resource manager for OSCARS.

=head1 SYNOPSIS

  use OSCARS::ResourceManager;

=head1 DESCRIPTION

Handles resources and authorizations associated with OSCARS.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 15, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

use SOAP::Lite;

use OSCARS::Logger;
use OSCARS::User;

sub new {
	my( $class, %args ) = @_;
	my( $self ) = { %args };

	bless( $self, $class );
	$self->initialize();
	return( $self );
}

sub initialize {
	my( $self ) = @_;

	$self->{clients} = {};
	my $authZ_factory = OSCARS::AuthZFactory->new();
	# currently only exists for AAAS; BSS depends on AAAS first doing AAA
	$self->{authZ} = $authZ_factory->instantiate($self->{database});

	my $authN_factory = OSCARS::AuthNFactory->new();
	# currently only exists for AAAS; BSS depends on AAAS first doing AAA
	$self->{authN} = $authN_factory->instantiate($self->{database});
	$self->{logger} = OSCARS::Logger->new();
} #____________________________________________________________________________


###############################################################################
# Adds SOAP::Lite client for given component to clients hash, indexed by
# component name.
#
sub add_client {
    my( $self, $as_num ) = @_;

    my( $statement, $client );

    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    # currently only handles one OSCARS server per domain
    if ($as_num) {
        $statement = 'SELECT * FROM clients WHERE as_num = ?';
        $client = $dbconn->get_row($statement, $as_num);
    }
    else {
	# local domain not given an AS number in the clients table
        $statement = 'SELECT * FROM clients WHERE as_num IS NULL';
        $client = $dbconn->get_row($statement);
    }
    $dbconn->disconnect();
    if (!$client) { return undef; }
    $self->{clients}->{component_name} = SOAP::Lite
                                        -> uri($client->{client_uri})
                                        -> proxy($client->{client_proxy});
    return $self->{clients}->{component_name};
} #____________________________________________________________________________


###############################################################################
# See if need to use client to forward to another machine
#
sub has_client {
    my( $self, $component_name ) = @_;

    if ($self->{clients}->{$component_name}) { return 1; }
    else { return 0; }
} #____________________________________________________________________________

###############################################################################
# Dispatch to server on another machine.
#
sub forward {
    my( $self, $component_name, $params ) = @_;

    my $som;
    if ( $self->{clients}->{$component_name} ) {
        $som = $self->{clients}->{$component_name}->dispatch($params);
    }
    else {
        $self->{logger}->add_string('Unable to forward; no such server');
        $self->{logger}->write_file('manager', $params->{method}, 1);
    }
    return $som;
} #____________________________________________________________________________


###############################################################################
# authenticate:  Attempts to authenticate user.  Currently will only succeed
#    with Login method.
#
sub authenticate {
    my( $self, $user, $params ) = @_;

    my( $status );

    if ( $self->{authN} ) {
        $status = $self->{authN}->authenticate($user, $params);
    }
    else {
        $status = 1;
        $user->set_authenticated($status);
    }
    return $status;
} #___________________________________________________________________________ 


###############################################################################
# authorized:  See if user has authorization to use a given resource.
sub authorized {
    my( $self, $user, $resource_name ) = @_;

    if ( $self->{authZ} ) {
        return $self->{authZ}->authorized($user, $resource_name);
    }
    else { return 1; }
} #___________________________________________________________________________ 


###############################################################################
# Only used by OSCARS tests.
#
sub get_test_account {
    my( $self, $role ) = @_;

    my $statement = 'SELECT user_login, user_password FROM users ' .
                    'WHERE user_login = ?';
    my $dbconn = OSCARS::Database->new();
    $dbconn->connect($self->{database});
    my $results = $dbconn->get_row($statement, $role);
    $dbconn->disconnect();
    return( $results->{user_login}, $results->{user_password} );
} #____________________________________________________________________________


###############################################################################
# write_exception:  Write exception to log.
#
sub write_exception {
    my( $self, $exception_text, $method_name ) = @_;

    $self->{logger}->add_string($exception_text);
    $self->{logger}->write_file('manager', $method_name, 1);
} #___________________________________________________________________________ 


#==============================================================================
package OSCARS::AuthZFactory;

use strict;
use Data::Dumper;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #___________________________________________________________________________ 


###############################################################################
#
sub instantiate {
    my( $self, $database ) = @_;

    my $authZ;

    my $location = 'OSCARS/' . $database . '/AuthZ.pm';
    my $class_name = 'OSCARS::' . $database . '::AuthZ';
    eval { require $location };
    if (!$@) { $authZ = $class_name->new('database' => $database); }
    else { $authZ = undef; }
    return $authZ;
} #___________________________________________________________________________ 




#==============================================================================
package OSCARS::AuthNFactory;

use strict;
use Data::Dumper;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #___________________________________________________________________________ 


###############################################################################
#
sub instantiate {
    my( $self, $database ) = @_;

    my $authN;

    my $location = 'OSCARS/' . $database . '/AuthN.pm';
    my $class_name = 'OSCARS::' . $database . '::AuthN';
    eval { require $location };
    if (!$@) { $authN = $class_name->new('database' => $database); }
    else { $authN = undef; }
    return $authN;
} #___________________________________________________________________________ 


######
1;
