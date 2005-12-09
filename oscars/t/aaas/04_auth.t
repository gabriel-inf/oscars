#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::AAAS::Auth;

my $auth = OSCARS::AAAS::Auth->new();
ok($auth);
