#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 3;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;
use OSCARS::Database;
use OSCARS::Intradomain::RouteHandler;

my $db_name = 'AAA';
my $aaa_component_name = 'AAA';
my $bss_component_name = 'Intradomain';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');

my $testdb = OSCARS::Database->new();
$testdb->connect('Intradomain');
my $rh = OSCARS::Intradomain::RouteHandler->new('user' => $testdb);
my $test_configs = $rh->get_test_configs('manageReservations');

my( $status, $msg, $reservation_id ) = createNSI(
                          $login, $password,
                          $test_configs->{reservation_source},
                          $test_configs->{reservation_destination});
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = queryNSI($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = cancelNSI($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub createNSI {
    my( $user_login, $user_password, $src, $dst ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'CreateNSI'; 

    $params{source_host} = $src;
    $params{destination_host} = $dst;

    $params{reservation_start_time} = time();
    $params{duration_hour} =       0.04;    # duration 5 minutes
    $params{reservation_time_zone} = "-08:00";

    # in Mbps
    $params{reservation_bandwidth} =      '10';
    $params{reservation_protocol} =       'udp';

    $params{reservation_description} =    'This is a test.';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . Dumper($results);
    return( 1, $msg, $results->{reservation_id} );
} #___________________________________________________________________________


#############################################################################
#
sub queryNSI {
    my ( $user_login, $user_password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'QueryNSI';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservation details:\n";
    $msg .= Dumper($results);
    return( 1, $msg );
} #___________________________________________________________________________



#############################################################################
#
sub cancelNSI {
    my( $user_login, $user_password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{server} = $bss_component_name;
    $params{user_login} = $user_login;
    $params{method} = 'CancelNSI';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    print STDERR Dumper($results);
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________
