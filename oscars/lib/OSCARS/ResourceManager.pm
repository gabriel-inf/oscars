# =============================================================================
package OSCARS::ResourceManager;

=head1 NAME

OSCARS::ResourceManager - resource manager for OSCARS.

=head1 SYNOPSIS

  use OSCARS::ResourceManager;

=head1 DESCRIPTION

Handles maintenance of user list and server startup.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

February 10, 2006

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

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
    $self->{admin} = OSCARS::User->new(
                            'dn' => 'admin',
                            'database' => $self->{database});
    my $authZ_factory = OSCARS::AuthZFactory->new();
    # currently only exists for AAAS; BSS depends on AAAS first doing AAA
    $self->{authZ} = $authZ_factory->instantiate($self->{database},
                                                 $self->{admin});

    my $authN_factory = OSCARS::AuthNFactory->new();
    # currently only exists for AAAS; BSS depends on AAAS first doing AAA
    $self->{authN} = $authN_factory->instantiate($self->{database});
    $self->{logger} = OSCARS::Logger->new();
} #____________________________________________________________________________


###############################################################################
# Gets information necessary to start SOAP::Lite daemon.
#
sub get_daemon_info {
    my( $self, $component_name ) = @_;

    my $statement = 'SELECT daemon_port FROM daemons WHERE daemon_component_name = ?';
    my $results = $self->{admin}->get_row($statement, $component_name);
    if (!$results) { return undef; }
    my $server_port = $results->{daemon_port};
    return $server_port;
} #____________________________________________________________________________


###############################################################################
# Adds SOAP::Lite client for given component to clients hash, indexed by
# component name.
#
sub add_client {
    my( $self, $component_name, $no_local ) = @_;

    my $statement = 'SELECT * FROM daemons WHERE daemon_component_name = ?';
    my $results = $self->{admin}->get_row($statement, $component_name);
    if (!$results) { return undef; }
    if ( ($results->{daemon_host_name} eq 'localhost' ) && $no_local ) {
        return undef;
    }
    my $server_port = $results->{daemon_port};
    my $uri = 'http://' . $results->{daemon_host_name} . ':' . $server_port .
             '/OSCARS/' . $results->{daemon_dispatcher_class};
    my $proxy = 'http://' . $results->{daemon_host_name} . ':' . $server_port .
             '/OSCARS/' . $results->{daemon_server_class};
    $self->{clients}->{component_name} = SOAP::Lite
                                        -> uri($uri)
                                        -> proxy($proxy);
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

    my $statement = 'SELECT user_dn, user_password FROM users ' .
                    'WHERE user_dn = ?';
    my $results = $self->{admin}->get_row($statement, $role);
    return( $results->{user_dn}, $results->{user_password} );
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
    my( $self, $database, $user ) = @_;

    my $authZ;

    my $location = 'OSCARS/' . $database . '/AuthZ.pm';
    my $class_name = 'OSCARS::' . $database . '::AuthZ';
    eval { require $location };
    if (!$@) { $authZ = $class_name->new('database' => $database,
		                         'user' => $user);
    }
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
