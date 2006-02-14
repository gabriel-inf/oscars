#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::ResourceManager;
use OSCARS::User;
use OSCARS::Method;

my $db_name = 'AAAS';
my $component_name = 'AAAS';

my $rm = OSCARS::ResourceManager->new('database' => $db_name);
my( $login, $password ) = $rm->get_test_account('user');
my $user = OSCARS::User->new(
                      'dn' => $login,
                      'database' => $db_name);

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $params = {};
$params->{server} = $component_name;
$params->{method} = 'Login';
$params->{user_dn} = $login;
$params->{user_password} = $password;

my $handler = $factory->instantiate( $user, $params );
ok($handler);
