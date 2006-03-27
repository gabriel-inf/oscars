#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::ResourceManager;
use OSCARS::AAAS::User;

my $db_name = 'AAAS';
my $rm = OSCARS::ResourceManager->new('database' => $db_name);
my $status = $rm->set_authentication_style('OSCARS::AAAS::AuthN', 'AAAS');

my( $login, $password ) = $rm->get_test_account('testaccount');
my $user = OSCARS::AAAS::User->new(
                      'login' => $login,
                      'database' => $db_name);
ok($user);

$status = $user->set_authorization_style('OSCARS::AAAS::AuthZ', 'AAAS');
ok($status);


