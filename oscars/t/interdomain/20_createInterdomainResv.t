#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::W3CDTF;

use Data::Dumper;

use TestManager;

my $testMgr = TestManager->new();

my $params = createInterdomainResvParams($testMgr);
my( $status, $results ) = $testMgr->dispatch('forward', $params);
ok($status);

$params = { 'tag' => $results->{tag} };


#############################################################################
#
sub createInterdomainResvParams {
    my( $testMgr ) = @_;

    my $epoch = time();
    my $f = DateTime::Format::W3CDTF->new;
    my $dt = DateTime->from_epoch( epoch => $epoch );
    my $offsetStr = "-07:00";
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
    return $params;
} #___________________________________________________________________________
