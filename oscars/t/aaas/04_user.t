#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::ResourceManager;

my $db_name = 'AAAS';
my $resource_manager = OSCARS::ResourceManager->new('database' => $db_name);
my( $login, $password ) = $resource_manager->get_test_account('user');
my $user = OSCARS::User->new(
                      'dn' => $login,
                      'database' => $db_name);
ok($user);
