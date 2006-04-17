#!/usr/bin/perl -w

use strict;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::ClientManager;

my $plugin_mgr = OSCARS::PluginManager->new();
my $authN = $plugin_mgr->use_plugin('authentication');
my $user_login = 'testaccount';
my $credentials  = $authN->get_credentials($user_login, 'password');

my $database = $plugin_mgr->get_database('Intradomain');
my $client_mgr = OSCARS::ClientManager->new('database' => $database);
my $client = $client_mgr->get_client();


my( $status, $msg ) = FindPendingReservations( $user_login, $credentials );
( $status, $msg ) = FindExpiredReservations( $user_login, $credentials );


#############################################################################
#
sub FindPendingReservations {
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );
    $params{component} = 'Intradomain';
    $params{method} = 'FindPendingReservations';
    $params{time_interval} = 20;

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
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{component} = 'Intradomain';
    $params{method} = 'FindExpiredReservations';
    $params{time_interval} = 20;

    my $som = $client->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
