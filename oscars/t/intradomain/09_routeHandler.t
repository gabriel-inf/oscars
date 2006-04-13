#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::Database;
use OSCARS::Intradomain::RouteHandler;

my $dbconn = OSCARS::Database->new();
$dbconn->connect('Intradomain');

my $rh = OSCARS::Intradomain::RouteHandler->new('user' => $dbconn);
ok($rh);
