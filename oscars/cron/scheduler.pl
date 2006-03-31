#!/usr/bin/perl -w

use strict;

use SOAP::Lite;

use OSCARS::ResourceManager;
use Data::Dumper;

my $db_name = 'AAAS';
my $bss_component_name = 'BSS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->use_authentication_plugin('OSCARS::AAAS::AuthN', $db_name);

my( $user_login, $user_password ) = $rm->get_test_account('testaccount');

my( $status, $msg ) = FindPendingReservations( $user_login, $user_password );
( $status, $msg ) = FindExpiredReservations( $user_login, $user_password );


#############################################################################
#
sub FindPendingReservations {
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'FindPendingReservations';
    $params{time_interval} = 20;

    my $som = $rm->add_client()->dispatch(\%params);
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

    $params{server} = $bss_component_name;
    $params{method} = 'FindExpiredReservations';
    $params{time_interval} = 20;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    $msg .= Dumper($results->{list});
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________
