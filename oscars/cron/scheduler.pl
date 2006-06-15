#!/usr/bin/perl -w

use strict;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::ClientManager;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);

# sign using user's certificate
$ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
$ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/userkey.pem";
# tells WSRF::Lite to sign the message with the above cert
$ENV{WSS_SIGN} = 'true';

my $clientMgr = OSCARS::ClientManager->new();

my( $status, $msg ) = reservationPending();
( $status, $msg ) = reservationExpired();


#############################################################################
#
sub reservationPending {

    my $params = {};
    $params->{timeInterval} = 20;
    my $methodName = 'reservationPending';

    my $client = $clientMgr->getClient($methodName);
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $request = SOAP::Data -> name($methodName . "Request" => $params );
    my $som = $client->call($method => $request);

    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub reservationExpired {

    my $params = {};
    $params->{timeInterval} = 20;

    my $methodName = 'reservationExpired';
    my $client = $clientMgr->getClient($methodName);
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $request = SOAP::Data -> name($methodName . "Request" => $params );
    my $som = $client->call($method => $request);

    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

