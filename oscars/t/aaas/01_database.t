#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::Database;

my $db_login = 'oscars';
my $password = 'ritazza6';
 
my $dbconn = OSCARS::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password);
ok($dbconn);
