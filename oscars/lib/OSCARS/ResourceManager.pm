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

December 21, 2005

=cut


use vars qw($VERSION);
$VERSION = '0.1';

use Data::Dumper;
use Error qw(:try);

use strict;

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

    $self->{users} = {};
    $self->{admin} = OSCARS::User->new( 'dn' => 'admin' );
    $self->{admin}->connect($self->{server_name});
    my $auth_factory = OSCARS::AuthFactory->new();
    $self->{authz} = $auth_factory->instantiate($self->{server_name});
} #____________________________________________________________________________


###############################################################################
# Gets information necessary to start SOAP::Lite daemon.
#
sub get_daemon_info {
    my( $self ) = @_;

    my $statement = 'SELECT server_port FROM servers' .
                    ' WHERE server_name = ?';
    my $results = $self->{admin}->do_query($statement, $self->{server_name});
    if (!@$results) { return undef; }
    my $len = scalar(@$results);
    # TODO:  error message
    if ($len > 1) { return undef; }

    return $results->[0]->{server_port};
} #____________________________________________________________________________


###############################################################################
# Gets information necessary to set up SOAP::Lite proxy.
#
sub get_proxy_info {
    my( $self, $server_name ) = @_;

    my $statement = 'SELECT server_uri, server_proxy FROM servers' .
                    ' WHERE server_name = ?';
    my $results = $self->{admin}->do_query($statement, $server_name);
    my $len = scalar(@$results);
    return( $results->[0]->{server_uri}, $results->[0]->{server_proxy} );
    # TODO: error message if > 1
    #if ($len == 1) {
        #return( $results->[0]->{server_uri}, $results->[0]->{server_proxy} );
    #}
    #return( undef, undef );
} #____________________________________________________________________________


###############################################################################
# Sets up SOAP::Lite proxy.
#
sub set_proxy {
    my( $self, $uri, $proxy ) = @_;

    $self->{proxy_server} = SOAP::Lite
                 -> uri($uri)
                 -> proxy($proxy);
    return $self->{proxy_server};
} #____________________________________________________________________________


###############################################################################
# Dispatch to server on another machine.
#
sub forward {
    my( $self, $params ) = @_;

    if ($self->{proxy_server}) {
        $self->{proxy_server}->dispatch($params);
    }
    # TODO:  error message if attempting to forward with non-existent proxy
} #____________________________________________________________________________


###############################################################################
# Gets user instance from user list if it exists; otherwise create an instance
# associated with the distinguished name given.
#
sub get_user {
    my( $self, $user_dn ) = @_;

    if (!$self->{users}->{$user_dn}) {
        $self->{users}->{$user_dn} = OSCARS::User->new( 'dn' => $user_dn );
    }
    return $self->{users}->{$user_dn};
} #____________________________________________________________________________


###############################################################################
# authenticate
#
sub authenticate {
    my( $self ) = @_;

    return 1;
} #___________________________________________________________________________ 


###############################################################################
# authorized
#
sub authorized {
    my( $self, $params ) = @_;

    if ($self->{authz}) { return $self->{authz}->authorized($params); }
    return 1;
} #___________________________________________________________________________ 


#==============================================================================
package OSCARS::AuthFactory;

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
    my( $self, $server_name ) = @_;

    my $authz;

    my $location = 'OSCARS/' . $server_name . '/AuthZ.pm';
    my $class_name = 'OSCARS::' . $server_name . '::AuthZ';
    eval { require $location };
    # create instance only if the module exists
    if (!$@) { $authz = $class_name->new(); }
    return $authz;
} #___________________________________________________________________________ 


######
1;
