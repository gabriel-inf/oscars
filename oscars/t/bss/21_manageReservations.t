#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 4;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $aaa_component_name = 'AAAS';
my $bss_component_name = 'BSS';
my $rm = OSCARS::ResourceManager->new( 'database' => $db_name);

my( $login, $password ) = $rm->get_test_account('user');

my( $status, $msg, $reservation_id ) = CreateReservation(
                          $login, $password,
                          'nettrash3.es.net', 'dc-cr1.es.net');
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = ManageReservations($login, $password);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = Reservation($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = CancelReservation($login, $password, $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub CreateReservation {
    my( $user_dn, $password, $src, $dst ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

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
sub ManageReservations {
    my ( $user_dn, $password ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{method} = 'ManageReservations';

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
sub Reservation {
    my ( $user_dn, $password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    $params{server} = $bss_component_name;
    $params{method} = 'Reservation';
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
    my( $user_dn, $password, $reservation_id ) = @_;

    # password necessary for test to run, but not for this method in general
    my %params = ('user_dn' => $user_dn, 'user_password' => $password );

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{server} = $bss_component_name;
    $params{user_dn} = $user_dn;
    $params{method} = 'ManageReservations';
    $params{op} = 'cancelReservation';
    $params{reservation_id} = $reservation_id;

    my $som = $rm->add_client()->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________
