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
my $client = $clientMgr->getClient();


my( $status, $msg ) = reservationPending( $login, $credentials );
( $status, $msg ) = reservationExpired( $login, $credentials );


#############################################################################
#
sub reservationPending {
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );
    $params{method} = 'reservationPending';
    $params{timeInterval} = 20;

    my $som = $client->reservationPending(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub reservationExpired {
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );

    $params{method} = 'reservationExpired';
    $params{timeInterval} = 20;

    my $som = $client->reservationExpired(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

