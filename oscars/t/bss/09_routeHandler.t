#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::Database;
use OSCARS::BSS::RouteHandler;

my $dbconn = OSCARS::Database->new();
$dbconn->connect('BSS');

my $rh = OSCARS::BSS::RouteHandler->new('user' => $dbconn);
ok($rh);
