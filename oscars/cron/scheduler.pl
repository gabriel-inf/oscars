#!/usr/bin/perl -w

use strict;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::ClientManager;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $configuration = $pluginMgr->getConfiguration();
my $database = $configuration->{database}->{'system'}->{location};

my $authN = $pluginMgr->usePlugin('authentication');
my $login = 'testaccount';

my $clientMgr = OSCARS::ClientManager->new('database' => $database);

my( $status, $msg ) = reservationPending( $login );
( $status, $msg ) = reservationExpired( $login );


#############################################################################
#
sub reservationPending {
    my ( $login ) = @_;

    my %params = ('login' => $login );
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
    my ( $login ) = @_;

    my %params = ('login' => $login );
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

