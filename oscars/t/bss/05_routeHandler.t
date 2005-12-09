#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::BSS::RouteHandler;

$rh = OSCARS::BSS::RouteHandler->new();
ok($rh);
