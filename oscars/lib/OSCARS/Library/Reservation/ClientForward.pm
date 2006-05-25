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

May 24, 2006

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
    my( $self, $request, $database ) = @_;

    print STDERR "next domain: $request->{nextDomain}\n";
    my $methodName = 'Forward';
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $clientMgr = OSCARS::ClientManager->new('database' => $database);
    my $client = $clientMgr->getClient($methodName, $request->{nextDomain});
    my $login = $clientMgr->getLogin($request->{nextDomain});

    if ( !$client ) {
        return( 'Unable to get client for next domain', undef );
    }
    my $payload = {};
    $payload->{request} = $request;
    $payload->{login} = $login;

    my $soapRequest = SOAP::Data -> name($methodName . "Request" => $payload );
    
    my $som = $client->call($method => $soapRequest);
    if ( !$som ) { return( 'Unable to make forwarding SOAP call', undef ); }
    if ($som->faultstring) { return( $som->faultstring, undef ); }
    return( undef, $som->result );
}


######
1;
# vim: et ts=4 sw=4
