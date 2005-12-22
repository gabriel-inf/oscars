#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::AAAS::AuthZ;

my $auth = OSCARS::AAAS::AuthZ->new();
ok($auth);
