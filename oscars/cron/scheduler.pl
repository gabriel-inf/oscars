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

my $database = $pluginMgr->getDatabase('Intradomain');
my $clientMgr = OSCARS::ClientManager->new('database' => $database);
my $client = $clientMgr->getClient();


my( $status, $msg ) = FindPendingReservations( $login, $credentials );
( $status, $msg ) = FindExpiredReservations( $login, $credentials );


#############################################################################
#
sub FindPendingReservations {
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );
    $params{component} = 'Intradomain';
    $params{method} = 'FindPendingReservations';
    $params{timeInterval} = 20;

    my $som = $client->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub FindExpiredReservations {
    my ( $login, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('login' => $login, 'password' => $password );

    $params{component} = 'Intradomain';
    $params{method} = 'FindExpiredReservations';
    $params{timeInterval} = 20;

    my $som = $client->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________

