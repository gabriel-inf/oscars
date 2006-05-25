#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use TestManager;

my $testMgr = TestManager->new();
ok( $testMgr->dispatch('listReservations') );
