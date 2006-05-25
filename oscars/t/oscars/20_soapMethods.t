#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 11;

use Data::Dumper;

use TestManager;
use OSCARS::Logger;

my $testMgr = TestManager->new();
ok( $testMgr );

ok( $testMgr->dispatch('userLogin') );
ok( $testMgr->dispatch('authorizationList') );
ok( $testMgr->dispatch('permissionList') );
ok( $testMgr->dispatch('resourceList') );
ok( $testMgr->dispatch('userQuery') );
ok( $testMgr->dispatch('userAdd') );
ok( $testMgr->dispatch('userModify') );
ok( $testMgr->dispatch('userRemove') );
ok( $testMgr->dispatch('userList') );
ok( $testMgr->dispatch('userLogout') );
