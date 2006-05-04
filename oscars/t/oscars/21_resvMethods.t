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
my( $status, $tag );

ok( listReservations($testMgr, $params->{listReservations}) );

( $status, $tag ) = createReservation( $testMgr, $params->{createReservation} );
ok( $status );

ok( queryReservation( $testMgr, $params->{queryReservation}, $tag ) );
ok( cancelReservation( $testMgr, $params->{cancelReservation}, $tag ) );
ok( reservationPending($testMgr, $params->{reservationPending}) );
ok( reservationExpired($testMgr, $params->{reservationExpired}) );


###############################################################################
#
sub listReservations {
    my ( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'listReservations');
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

    my( $status, $results ) = $testMgr->dispatch($params, 'createReservation');
    return( $status, $results->{tag} );
} #___________________________________________________________________________


#############################################################################
#
sub queryReservation {
    my ( $testMgr, $params, $reservationTag ) = @_;

    $params->{tag} = $reservationTag;
    return $testMgr->dispatch($params, 'queryReservation');
} #___________________________________________________________________________


#############################################################################
# Delete the reservation with the given tag (set its status
# to cancelled).
sub cancelReservation {
    my( $testMgr, $params, $reservationTag ) = @_;

    $params->{tag} = $reservationTag;
    return $testMgr->dispatch($params, 'cancelReservation');
} #___________________________________________________________________________


#############################################################################
#
sub reservationPending {
    my ( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'reservationPending');
} #___________________________________________________________________________


#############################################################################
#
sub reservationExpired {
    my ( $testMgr, $params ) = @_;

    return $testMgr->dispatch($params, 'reservationExpired');
} #___________________________________________________________________________
