#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;

use OSCARS::BSS::Database;
use OSCARS::BSS::UpdateRouterTables;

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSSTest',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);

my $ru = OSCARS::BSS::UpdateRouterTables->new('dbconn' => $dbconn);
ok($ru);

my %params;

$params{directory} = '/home/davidr/ifrefpoll';
my $results = $ru->update_router_info(\%params);
ok( $results );

