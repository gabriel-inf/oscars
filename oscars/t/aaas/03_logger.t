#!/usr/bin/perl

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::AAAS::Logger;

my $logger = OSCARS::AAAS::Logger->new();
ok($logger);
