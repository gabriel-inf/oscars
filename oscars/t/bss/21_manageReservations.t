#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 4;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;
use OSCARS::Database;
use OSCARS::BSS::RouteHandler;

my $db_name = 'AAAS';
my $aaa_component_name = 'AAAS';
my $bss_component_name = 'BSS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);
my $aaa_status = $rm->set_authentication_style('OSCARS::AAAS::AuthN', 'AAAS');

my( $login, $password ) = $rm->get_test_account('testaccount');

my $testdb = OSCARS::Database->new();
$testdb->connect('BSS');
my $rh = OSCARS::BSS::RouteHandler->new('user' => $testdb);
my $test_configs = $rh->get_test_configs('manageReservations');

my( $status, $msg, $reservation_id ) = CreateReservation(
                          $login, $password,
                          $test_configs->{reservation_source},
                          $test_configs->{reservation_destination});
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = ViewReservations($login, $password);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = ReservationDetails($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = CancelReservation($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub CreateReservation {
    my( $user_login, $user_password, $src, $dst ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'ManageReservations'; 
    $params{op} = 'createReservation'; 

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


###############################################################################
#
sub ViewReservations {
    my ( $user_login, $user_password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'ManageReservations';
    $params{op} = 'viewReservations';

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservations:\n";
    $msg .= Dumper($results);
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________



#############################################################################
#
sub ReservationDetails {
    my ( $user_login, $user_password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    $params{server} = $bss_component_name;
    $params{method} = 'ReservationDetails';
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
sub CancelReservation {
    my( $user_login, $user_password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_login' => $user_login, 'user_password' => $user_password );

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{server} = $bss_component_name;
    $params{user_login} = $user_login;
    $params{method} = 'ManageReservations';
    $params{op} = 'cancelReservation';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    print STDERR Dumper($results);
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________
