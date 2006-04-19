#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Method;

my $login = 'testaccount';
my $component = 'AAA';
my $mgr = OSCARS::PluginManager->new();
my $authN = $mgr->usePlugin('authentication');

my $credentials = $authN->getCredentials($login, 'password');
my $user = $authN->getUser($login);

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $params = {};
$params->{component} = $component;
$params->{method} = 'Login';
$params->{login} = $login;
$params->{user_password} = $credentials;

my $handler = $factory->instantiate( $user, $params );
ok($handler);
