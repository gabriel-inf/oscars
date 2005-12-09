#!/usr/bin/perl

use strict;
use Test::Simple tests => 1;

use OSCARS::AAAS::Logger;

my $logger = OSCARS::AAAS::Logger->new('dir' => '/home/oscars/logs', 
                                       'method' => 'test');
ok($logger);
