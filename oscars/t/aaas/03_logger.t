#!/usr/bin/perl

use strict;
use Test::Simple tests => 1;

use OSCARS::Logger;

my $logger = OSCARS::Logger->new('dir' => '/home/oscars/logs', 
                                       'method' => 'test');
ok($logger);
