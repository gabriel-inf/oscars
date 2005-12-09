#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::BSS::Policy;

my $policy = OSCARS::BSS::Policy->new();
ok($policy);
