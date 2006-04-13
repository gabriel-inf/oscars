#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::ResourceManager;
use OSCARS::AAA::User;

my $db_name = 'AAA';
my $rm = OSCARS::ResourceManager->new('database' => $db_name);
my $status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');
my $user = OSCARS::AAA::User->new(
                      'login' => $login,
                      'database' => $db_name);
ok($user);

$status = $user->use_authorization_plugin('OSCARS::AAA::AuthZ', 'AAA');
ok($status);


