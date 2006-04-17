#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 3;
use Data::Dumper;

use TestManager;

my $test_mgr = TestManager->new();

my( $status, $msg, $reservation_id ) = createNSI( $test_mgr );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = queryNSI( $test_mgr, $reservation_id );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = cancelNSI( $test_mgr, $reservation_id );
ok( $status, $msg );
print STDERR $msg;


#############################################################################
#
sub createNSI {
    my( $test_mgr ) = @_;

    my $params = $test_mgr->get_params('intradomain/21_createNSI.xml');
    my $test_configs = $test_mgr->get_intradomain_configs('manageReservations');
    $params->{source_host} = $test_configs->{reservation_source};
    $params->{destination_host} = $test_configs->{reservation_destination};
    $params->{reservation_start_time} = time();

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . Dumper($results);
    return( 1, $msg, $results->{reservation_id} );
} #___________________________________________________________________________


#############################################################################
#
sub queryNSI {
    my ( $test_mgr, $reservation_id ) = @_;

    my $params = $test_mgr->get_params('intradomain/21_queryNSI.xml');
    $params->{reservation_id} = $reservation_id;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    my $msg = "\nReservation details:\n";
    $msg .= Dumper($results);
    return( 1, $msg );
} #___________________________________________________________________________



#############################################################################
# Delete the reservation with the given id (set its status
# to cancelled).
sub cancelNSI {
    my( $test_mgr, $reservation_id ) = @_;

    my $params = $test_mgr->get_params('intradomain/21_cancelNSI.xml');
    $params->{reservation_id} = $reservation_id;

    my $som = $test_mgr->dispatch($params);
    if ($som->faultstring) { return( 0, $som->faultstring ); }

    my $results = $som->result;
    print STDERR Dumper($results);
    my $msg = "\nSuccessfully cancelled reservation $results->{reservation_id}\n";
    return( 1, $msg );
} #___________________________________________________________________________
