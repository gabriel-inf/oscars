#!/usr/bin/perl

use Test::Simple tests => 2;

use OSCARS::PluginManager;
use OSCARS::Method;

my $login = 'testaccount';
my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $authN = $pluginMgr->usePlugin('authentication');

my $credentials = $authN->getCredentials($login, 'password');
my $user = $authN->getUser($login);

my $factory = OSCARS::MethodFactory->new('pluginMgr' => $pluginMgr);
ok($factory);

my $method = 'UserLogin';

my $handler = $factory->instantiate( $user, $method );
ok($handler);
