#!/usr/bin/perl -w

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::AAAS::Auth;

$auth = OSCARS::AAAS::Auth->new();
ok($auth);
