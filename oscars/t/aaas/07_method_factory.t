#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::ResourceManager;
use OSCARS::Method;
use OSCARS::AAA::User;

my $db_name = 'AAA';
my $component_name = 'AAA';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
my $status = $rm->use_authentication_plugin('OSCARS::AAA::AuthN', 'AAA');

my( $login, $password ) = $rm->get_test_account('testaccount');
my $user = OSCARS::AAA::User->new(
                      'login' => $login,
		      'database' => $db_name);
$status = $user->use_authorization_plugin('OSCARS::AAA::AuthZ', 'AAA');

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $params = {};
$params->{server} = $component_name;
$params->{method} = 'Login';
$params->{user_login} = $login;
$params->{user_password} = $password;

my $handler = $factory->instantiate( $user, $params );
ok($handler);
