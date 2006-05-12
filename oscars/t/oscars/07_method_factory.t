#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Method;

my $login = 'testaccount';
my $mgr = OSCARS::PluginManager->new();
my $authN = $mgr->usePlugin('authentication');

my $credentials = $authN->getCredentials($login, 'password');
my $user = $authN->getUser($login);

my $factory = OSCARS::MethodFactory->new();
ok($factory);

my $method = 'userLogin';
my $request = {};
$request->{login} = $login;
$request->{password} = $credentials;

my $handler = $factory->instantiate( $user, $method );
ok($handler);
