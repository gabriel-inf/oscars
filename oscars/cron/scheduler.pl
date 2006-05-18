#!/usr/bin/perl -w

use strict;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::ClientManager;

my $pluginMgr = OSCARS::PluginManager->new();
my $authN = $pluginMgr->usePlugin('authentication');
my $login = 'testaccount';
my $credentials  = $authN->getCredentials($login, 'password');

my $database = $pluginMgr->getLocation('system');
my $clientMgr = OSCARS::ClientManager->new('database' => $database);

my( $status, $msg ) = reservationPending( $login, $credentials );
( $status, $msg ) = reservationExpired( $login, $credentials );


#############################################################################
#
sub reservationPending {
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );
    $params{timeInterval} = 20;
    my $methodName = 'reservationPending';

    my $client = $clientMgr->getClient($methodName);
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $request = SOAP::Data -> name($methodName . "Request" => \%params );
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
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );
    $params{timeInterval} = 20;

    my $methodName = 'reservationExpired';
    my $client = $clientMgr->getClient($methodName);
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $request = SOAP::Data -> name($methodName . "Request" => \%params );
    my $som = $client->call($method => $request);

    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

