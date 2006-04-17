#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Method;

my $user_login = 'testaccount';
my $component_name = 'AAA';
my $mgr = OSCARS::PluginManager->new();
my $authN = $mgr->use_plugin('authentication');

my $credentials = $authN->get_credentials($user_login, 'password');
my $user = $authN->get_user($user_login);

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $params = {};
$params->{component} = $component_name;
$params->{method} = 'Login';
$params->{user_login} = $user_login;
$params->{user_password} = $credentials;

my $handler = $factory->instantiate( $user, $params );
ok($handler);
