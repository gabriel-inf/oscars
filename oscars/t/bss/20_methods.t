#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 8;

use SOAP::Lite;
use Data::Dumper;

use OSCARS::ResourceManager;

my( $status, $msg, $reservation_id );
my $server_name = 'BSS';
my $resource_manager = OSCARS::ResourceManager->new(
                                                'server_name' => $server_name);
ok($resource_manager);

my( $uri, $proxy ) = $resource_manager->get_proxy_info($server_name);
my $soap_proxy = $resource_manager->set_proxy( $uri, $proxy );
ok($soap_proxy);

( $status, $msg ) = ViewReservations('dwrobertson@lbl.gov', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg, $reservation_id ) = CreateReservation(
                          'dwrobertson@lbl.gov', '15',
                          'nettrash3.es.net', 'dc-cr1.es.net');
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = ViewDetails('dwrobertson@lbl.gov', 15, $reservation_id);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = test_scheduling_reservations(
                          'FindPendingReservations', 'scheduler', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = test_scheduling_reservations(
                          'FindExpiredReservations', 'scheduler', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = CancelReservation('dwrobertson@lbl.gov', 15,
                                       $reservation_id);
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub CreateReservation {
    my( $user_dn, $user_level, $src, $dst ) = @_;

    my %params;

    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{source_host} = $src;
    $params{destination_host} = $dst;

    $params{reservation_start_time} = time();
    $params{duration_hour} =       0.04;    # duration 5 minutes
    $params{reservation_time_zone} = "-08:00";

    # in Mbps
    $params{reservation_bandwidth} =      '10';
    $params{reservation_protocol} =       'udp';

    $params{reservation_description} =    'This is a test.';
    $params{server_name} = 'BSS';
    $params{method} = 'CreateReservation'; 

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . to_string($results);
    return( 1, $msg, $results->{reservation_id} );
} #___________________________________________________________________________


#############################################################################
#
sub CancelReservation {
    my( $user_dn, $user_level, $reservation_id ) = @_;

    my %params;

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{reservation_id} = $reservation_id;
    $params{server_name} = 'BSS';
    $params{method} = 'CancelReservation';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub ViewReservations {
    my ( $user_dn, $user_level ) = @_;

    my %params;

    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{server_name} = 'BSS';
    $params{method} = 'ViewReservations';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservations:\n";
    for my $row (@$results) {
        $msg .= to_string($row);
    }
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub ViewDetails {
    my ( $user_dn, $user_level, $reservation_id ) = @_;

    my %params ;

    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{reservation_id} = $reservation_id;
    $params{server_name} = 'BSS';
    $params{method} = 'ViewDetails';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservation details:\n";
    $msg .= to_string($results);
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub test_scheduling_reservations {
    my ( $method_name, $user_dn, $user_level ) = @_;

    my %params;

    $params{method} = $method_name;
    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{time_interval} = 20;
    $params{server_name} = 'BSS';

    my $som = $soap_proxy->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }
    my $results = $som->result;

    my $msg = "\nReservations handled:\n";
    for my $row (@$results) {
        $msg .= to_string($row);
    }
    $msg .= "\n";
    return( 1, $msg );
} #___________________________________________________________________________


##############################################################################
#
sub to_string {
    my( $results ) = @_;

    my( $key, $value );

    my $msg = '';
    foreach $key(sort keys %{$results} ) {
        if (($key ne 'status_msg') &&
            defined($results->{$key})) {
            $value = $results->{$key};
            if ($value) { $msg .= "$key -> $value\n"; }
            else { $msg .= "$key -> \n"; }
        }
    }
    return $msg;
} #___________________________________________________________________________
