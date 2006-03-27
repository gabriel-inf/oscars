#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::ResourceManager;
use OSCARS::Method;
use OSCARS::AAAS::User;

my $db_name = 'AAAS';
my $component_name = 'AAAS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
my $status = $rm->set_authentication_style('OSCARS::AAAS::AuthN', 'AAAS');

my( $login, $password ) = $rm->get_test_account('testaccount');
my $user = OSCARS::AAAS::User->new(
                      'login' => $login,
		      'database' => $db_name);
$status = $user->set_authorization_style('OSCARS::AAAS::AuthZ', 'AAAS');

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $params = {};
$params->{server} = $component_name;
$params->{method} = 'Login';
$params->{user_login} = $login;
$params->{user_password} = $password;

my $handler = $factory->instantiate( $user, $params );
ok($handler);
