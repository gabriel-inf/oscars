#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 6;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::W3CDTF;

use Data::Dumper;

use TestManager;

my $testMgr = TestManager->new();
my $params = $testMgr->getParams('oscars/params.xml');
my( $status, $msg, $reservationTag );

($status, $msg) = listReservations($testMgr, $params->{listReservations});
ok($status, $msg);
print STDERR $msg;

( $status, $msg, $reservationTag ) = createReservation( $testMgr, $params->{createReservation} );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) = queryReservation( $testMgr, $params->{queryReservation}, $reservationTag );
ok( $status, $msg );
print STDERR $msg;

( $status, $msg ) =
    cancelReservation( $testMgr, $params->{cancelReservation}, $reservationTag );
ok( $status, $msg );
print STDERR $msg;

($status, $msg) =
    reservationPending($testMgr, $params->{reservationPending});
ok($status, $msg);
print STDERR $msg;

($status, $msg) =
     reservationExpired($testMgr, $params->{reservationExpired});
ok($status, $msg);
print STDERR $msg;


###############################################################################
#
sub listReservations {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'listReservations');
    if ( $errorMsg ) { return( 0, $errorMsg ); }
    my $msg = "\nReservations:\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub createReservation {
    my( $testMgr, $params ) = @_;

    my $testConfigs =
        $testMgr->getReservationConfigs('createReservation');
    $params->{srcHost} = $testConfigs->{reservation_source};
    $params->{destHost} = $testConfigs->{reservation_destination};

    my $epoch = time();
    my $f = DateTime::Format::W3CDTF->new;
    my $dt = DateTime->from_epoch( epoch => $epoch );
    my $offsetStr = $params->{origTimeZone};
    # strip out semicolon
    $offsetStr =~ s/://;
    my $timezone = DateTime::TimeZone->new( name => $offsetStr );
    $dt->set_time_zone($timezone);
    $params->{startTime} = $f->format_datetime($dt);

    # end time is 4 minutes later
    $epoch += 240;
    $dt = DateTime->from_epoch( epoch => $epoch );
    $dt->set_time_zone($timezone);
    $params->{endTime} = $f->format_datetime($dt);

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'createReservation');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nStatus: Your reservation has been processed successfully.\n";
    $msg .= "Details:\n" . Dumper($results);
    return( 1, $msg, $results->{tag} );
} #___________________________________________________________________________


#############################################################################
#
sub queryReservation {
    my ( $testMgr, $params, $reservationTag ) = @_;

    $params->{tag} = $reservationTag;
    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'queryReservation');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservation details:\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
# Delete the reservation with the given tag (set its status
# to cancelled).
sub cancelReservation {
    my( $testMgr, $params, $reservationTag ) = @_;

    $params->{tag} = $reservationTag;
    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'cancelReservation');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nSuccessfully cancelled reservation $params->{tag}\n";
    $msg .= "\nDetails:\n" . Dumper($results) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub reservationPending {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'reservationPending');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservations handled:\n" . Dumper($results->{list}) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________


#############################################################################
#
sub reservationExpired {
    my ( $testMgr, $params ) = @_;

    my( $errorMsg, $results ) = $testMgr->dispatch($params, 'reservationExpired');
    if ( $errorMsg ) { return( 0, $errorMsg ); }

    my $msg = "\nReservations handled:\n" . Dumper($results->{list}) . "\n";
    return( 1, $msg );
} #___________________________________________________________________________
