#==============================================================================
package OSCARS::Library::Reservation::ClientForward;

=head1 NAME

OSCARS::Library::Reservation::ClientForward - Forward a request to another domain.

=head1 SYNOPSIS

  use OSCARS::Library::Reservation::ClientForward;

=head1 DESCRIPTION

Forward a request to another domain (currently only OSCARS/BRUW).

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov),

=head1 LAST MODIFIED

June 8, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::ClientManager;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };

    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

} #____________________________________________________________________________


sub forward {
    my( $self, $request, $database, $logger ) = @_;

    my $methodName = 'forward';
    print STDERR "next domain: $request->{nextDomain}\n";
    my $clientMgr = OSCARS::ClientManager->new('database' => $database);
    my $client = $clientMgr->getClient($methodName, $request->{nextDomain});
    if ( !$client ) {
        $logger->info("forwarding.error",
                      { 'error' => "No such domain $request->{nextDomain}" });
        return undef;
    }

    $logger->info("forwarding.start", $request );
    my $forwardRequest = {};
    for my $key (keys %{$request}) {
        $forwardRequest->{$key} = $request->{$key};
    }
    if ( $request->{ingressRouterIP} ) {
        $forwardRequest->{ingressRouterIP} = undef;
    }
    if ( $request->{egressRouterIP} ) {
        $forwardRequest->{srcHost} = $request->{egressRouterIP};
        $forwardRequest->{egressRouterIP} = undef;
    }
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $login = $clientMgr->getLogin($request->{nextDomain});
    my $payload = {};
    $payload->{request} = $forwardRequest;
    $payload->{login} = $login;

    my $soapRequest = SOAP::Data -> name($methodName . "Request" => $payload );
    my $som = $client->call($method => $soapRequest);
    if (!$som) {
        throw Error::Simple("Unable to make forwarding SOAP call");
    }
    if ( $som->faultstring ) {
	my $msg = $som->faultstring;
        throw Error::Simple("Unable to forward: $msg");
    }
    $logger->info("forwarding.finish", $som->result );
    return( $som->result );
}


######
1;
# vim: et ts=4 sw=4
