#!/usr/bin/perl -w

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::AAAS::Database;

my $db_login = 'oscars';
my $password = 'ritazza6';
 
my $dbconn = OSCARS::AAAS::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password);
ok($dbconn);
