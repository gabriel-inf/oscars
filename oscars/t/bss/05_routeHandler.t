#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 3;

use OSCARS::Logger;
use OSCARS::Database;
use OSCARS::BSS::RouteHandler;

my $logger = OSCARS::Logger->new( 'dir' => '/home/oscars/logs',
                                        'method' => 'test');
ok($logger);

my $dbconn = OSCARS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);
$dbconn->connect('BSS');

my $rh = OSCARS::BSS::RouteHandler->new('user' => $dbconn);
ok($rh);
$logger->end_log('test', '');
