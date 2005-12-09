#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::BSS::Database;

my $db_login = 'oscars';
my $password = 'ritazza6';
 
my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password);
ok($dbconn);
