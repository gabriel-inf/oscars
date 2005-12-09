#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 3;

use OSCARS::AAAS::Logger;
use OSCARS::BSS::Database;
use OSCARS::BSS::RouteHandler;

my %params = ( 'method' => 'test' );
my $logger = OSCARS::AAAS::Logger->new(
                               'dir' => '/home/davidr/oscars/tmp',
                               'params' => \%params);
ok($logger);
$logger->start_log();

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);

my $rh = OSCARS::BSS::RouteHandler->new('dbconn' => $dbconn);
ok($rh);
