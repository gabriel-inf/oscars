#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::PluginManager;

my $configFile = $ENV{HOME} . '/.oscars.xml';
my $pluginMgr = OSCARS::PluginManager->new('location' => $configFile);
my $login = 'testaccount';
my $authN = $pluginMgr->usePlugin('authentication');

my $credentials = $authN->getCredentials($login, 'certificate');
my $user = $authN->getUser($login);
ok( $user );
