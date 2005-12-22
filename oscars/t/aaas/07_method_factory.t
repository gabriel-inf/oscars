#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::Method;

my $factory = OSCARS::MethodFactory->new();
ok($factory);
