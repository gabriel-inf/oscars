#==============================================================================
package OSCARS::Server;

use SOAP::Transport::HTTP;

use strict;
use Data::Dumper;

use OSCARS::ResourceManager;

my $len = scalar(@ARGV);
if ($len != 1) {
    print STDERR "Usage:\t\tperl Server.pm component_name\n";
    print STDERR "Example:\tperl Server.pm AAAS\n";
    exit;
}

my $server_name = $ARGV[0];
my $resource_manager;

if ($server_name) {
    start_server($server_name);
}

###############################################################################
#
sub start_server {
    my ( $server_name ) = @_;

    $resource_manager = 
                   OSCARS::ResourceManager->new('server_name' => $server_name);
    my $portnum = $resource_manager->get_daemon_info();

    # set up proxy if it will be necessary to forward requests
    # TODO:  fix hard-wiring BSS
    my( $uri, $proxy ) = $resource_manager->get_proxy_info('BSS');
    if ($proxy) { $resource_manager->set_proxy($uri, $proxy); }

    if ($server_name) {
        my $daemon = SOAP::Transport::HTTP::Daemon
            -> new (LocalPort => $portnum, Listen => 5, Reuse => 1)
            -> dispatch_to('OSCARS::Dispatcher');
        $daemon->handle;
    }
} #____________________________________________________________________________


#==============================================================================
package OSCARS::Dispatcher;

=head1 NAME

OSCARS::Dispatcher - SOAP::Lite dispatcher for OSCARS.

=head1 SYNOPSIS

  use OSCARS::Dispatcher;

=head1 DESCRIPTION

Dispatcher for SOAP::Lite.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 21, 2005

=cut

use Error qw(:try);
use Data::Dumper;
use SOAP::Lite;

use strict;

use OSCARS::ResourceManager;
use OSCARS::Logger;
use OSCARS::Method;


###############################################################################
#
sub dispatch {
    my ( $class_name, $params ) = @_;

    my( $ex );
    my $results = {};
    my( $user, $handler );

    my $logger = OSCARS::Logger->new( 'dir' => '/home/oscars/logs');
    try {
        if (!$resource_manager->authenticate($params)) {
            throw Error::Simple(
                "User $params->{user_dn} not able to authenticate to make $params->{method} call");
        }
        if (!$resource_manager->authorized($params)) {
            throw Error::Simple(
                "User $params->{user_dn} not authorized to make $params->{method} call");
        }
        # if handler is present on this server
        if ($server_name eq $params->{server_name}) {
            $user = $resource_manager->get_user($params->{user_dn});
            $user->connect($server_name);
            my $factory = OSCARS::MethodFactory->new();
            $handler = $factory->instantiate( $user, $params );
            my $err = $handler->validate();
            if ($err) { throw Error::Simple($err); }
            # call SOAP method
            $results = $handler->soap_method();
         }
         # Method is not on this server.  Forward to the correct one.
         else {
             my $som = $resource_manager->forward($params);
             $results = $som->result;
         }
    }
    catch Error::Simple with { $ex = shift; }
    otherwise { $ex = shift; }
    finally {
        if ($ex) {
            print STDERR $ex->{-text}, "\n";
            $logger->write_log($ex->{-text});
                # caught by SOAP to indicate fault
            die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
        }
        elsif ($handler) { $handler->post_process($results); }
    };
    return $results;
} #____________________________________________________________________________


######
1;
