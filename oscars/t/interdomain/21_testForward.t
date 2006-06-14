#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::W3CDTF;

use Data::Dumper;

use TestManager;
use OSCARS::PluginManager;

my $testMgr = TestManager->new();
my $params = createTestForwardParams($testMgr);

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $authN = $pluginMgr->usePlugin('authentication');
my $password = $authN->getCredentials('testaccount', 'password');
$params->{password} = $password;
print STDERR Dumper($params);

my( $status, $results ) = $testMgr->dispatch('testForward', $params);
ok($status);

$params = { 'tag' => $results->{tag} };


#############################################################################
#
sub createTestForwardParams {
    my( $testMgr ) = @_;

    my $testConfigs =
        $testMgr->getReservationConfigs('createInterdomainResv');
    my $params = {};
    $params->{srcHost} = $testConfigs->{reservation_source};
    $params->{destHost} = $testConfigs->{reservation_destination};
    $params->{egressRouterIP} = $testConfigs->{egress_loopback};

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
