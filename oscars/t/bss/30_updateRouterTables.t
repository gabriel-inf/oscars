#!/usr/bin/perl

use strict;
use Test::Simple tests => 3;

use OSCARS::Database;
use OSCARS::BSS::Method::UpdateRouterTables;

my $dbconn = OSCARS::Database->new(
                 'database' => 'DBI:mysql:BSSTest',
                 'dblogin' => 'oscars',
                 'password' => 'ritazza6');
ok($dbconn);

my $ru = OSCARS::BSS::Method::UpdateRouterTables->new('user' => $dbconn);
ok($ru);

my %params;

$params{directory} = '/home/davidr/ifrefpoll';
my $results = $ru->update_router_info(\%params);
ok( $results );

