#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 3;

use OSCARS::AAAS::Logger;
use OSCARS::BSS::Database;
use OSCARS::BSS::RouteHandler;

my $logger = OSCARS::AAAS::Logger->new( 'dir' => '/home/oscars/logs',
                                        'method' => 'test');
ok($logger);
$logger->start_log();

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);

my $rh = OSCARS::BSS::RouteHandler->new('dbconn' => $dbconn);
ok($rh);
$logger->end_log();
