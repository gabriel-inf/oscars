#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::Logger;
use OSCARS::Database;
use OSCARS::BSS::RouteHandler;

my $logger = OSCARS::Logger->new();

my $dbconn = OSCARS::Database->new();
$dbconn->connect('BSS');

my $rh = OSCARS::BSS::RouteHandler->new('user' => $dbconn);
ok($rh);
