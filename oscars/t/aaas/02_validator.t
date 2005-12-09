#!/usr/bin/perl

use strict;
use Test::Simple tests => 1;

use OSCARS::AAAS::Validator;

my $validator = OSCARS::AAAS::Validator->new();
ok($validator);
