#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 7;

use SOAP::Lite;
use Data::Dumper;

my( $status, $msg, $reservation_id );

my $soap_server = SOAP::Lite
    ->uri('http://localhost:3000/OSCARS/BSS/Dispatcher')
    ->proxy('http://localhost:3000/bss');
ok($soap_server);

( $status, $msg ) = view_reservations($soap_server,
                          'dwrobertson@lbl.gov', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg, $reservation_id ) = create_reservation($soap_server,
                          'dwrobertson@lbl.gov', '15',
                          'nettrash3.es.net', 'dc-cr1.es.net');
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = view_details($soap_server, 'dwrobertson@lbl.gov', 15,
                                   $reservation_id);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = test_scheduling_reservations($soap_server,
                          'find_pending_reservations', 'scheduler', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = test_scheduling_reservations($soap_server,
                          'find_expired_reservations', 'scheduler', 15);
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = cancel_reservation($soap_server, 'dwrobertson@lbl.gov',
                                       15, $reservation_id);
ok( $status, $msg );
print STDERR $msg;

#############################################################################
#
sub create_reservation {
    my( $soap_server, $user_dn, $user_level, $src, $dst ) = @_;

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
    $params{method} = 'create_reservation'; 

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . to_string($results);
    return( 1, $msg, $results->{reservation_id} );
} #___________________________________________________________________________


#############################################################################
#
sub cancel_reservation {
    my( $soap_server, $user_dn, $user_level, $reservation_id ) = @_;

    my %params;

    # Delete the reservation with the given id (set its status
    # to cancelled).
    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{reservation_id} = $reservation_id;
    $params{server_name} = 'BSS';
    $params{method} = 'cancel_reservation';

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub view_reservations {
    my ( $soap_server, $user_dn, $user_level ) = @_;

    my %params;

    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{server_name} = 'BSS';
    $params{method} = 'view_reservations';

    my $som = $soap_server->dispatch(\%params);
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
sub view_details {
    my ( $soap_server, $user_dn, $user_level, $reservation_id ) = @_;

    my %params ;

    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{reservation_id} = $reservation_id;
    $params{server_name} = 'BSS';
    $params{method} = 'view_details';

    my $som = $soap_server->dispatch(\%params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservation details:\n";
    $msg .= to_string($results);
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub test_scheduling_reservations {
    my ( $soap_server, $method_name, $user_dn, $user_level ) = @_;

    my %params;

    $params{method} = $method_name;
    $params{user_dn} = $user_dn;
    $params{user_level} = $user_level;
    $params{time_interval} = 20;
    $params{server_name} = 'BSS';

    my $som = $soap_server->dispatch(\%params);
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
