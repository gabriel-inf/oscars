#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 5;

use DateTime;
use DateTime::Format::W3CDTF;

use Data::Dumper;

use TestManager;

my $testMgr = TestManager->new();
my $params = $testMgr->getParams('intradomain/params.xml');
my( $status, $msg, $reservationId );

#($status, $msg) = listReservations($testMgr, $params->{listReservations});
#ok($status, $msg);
#print STDERR $msg;

( $status, $msg, $reservationId ) = createReservation( $testMgr, $params->{createReservation} );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = queryReservation( $testMgr, $params->{queryReservation}, $reservationId );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) =
    cancelReservation( $testMgr, $params->{cancelReservation}, $reservationId );
ok( $status, $msg );
print STDERR $msg;

($status, $msg) =
    findPendingReservations($testMgr, $params->{findPendingReservations});
ok($status, $msg);
print STDERR $msg;

($status, $msg) =
     findExpiredReservations($testMgr, $params->{findExpiredReservations});
ok($status, $msg);
print STDERR $msg;


###############################################################################
#
sub listReservations {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nReservations:\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub createReservation {
    my( $testMgr, $params ) = @_;

    my $testConfigs =
        $testMgr->getIntradomainConfigs('createReservation');
    $params->{srcHost} = $testConfigs->{reservation_source};
    $params->{destHost} = $testConfigs->{reservation_destination};

    my $epoch = time();
    my $f = DateTime::Format::W3CDTF->new;
    my $dt = DateTime->from_epoch( epoch => $epoch );
    #$params->{startTime} = $f->format_datetime($dt);
    $params->{startTime} = $epoch;

    # end time is 4 minutes later
    $epoch += 240;
    $dt = DateTime->from_epoch( epoch => $epoch );
    #$params->{endTime} = $f->format_datetime($dt);
    $params->{endTime} = $epoch;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . Dumper($results);
    return( 1, $msg, $results->{id} );
} #___________________________________________________________________________


#############################################################################
#
sub queryReservation {
    my ( $testMgr, $params, $reservationId ) = @_;

    $params->{id} = $reservationId;
    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservation details:\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
# Delete the reservation with the given id (set its status
# to cancelled).
sub cancelReservation {
    my( $testMgr, $params, $reservationId ) = @_;

    $params->{id} = $reservationId;
    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nSuccessfully cancelled reservation $results->{id}\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub findPendingReservations {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservations handled:\n" . Dumper($results->{list}) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub findExpiredReservations {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params);
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservations handled:\n" . Dumper($results->{list}) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________
