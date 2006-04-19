#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::PluginManager;

my $login = 'testaccount';
my $mgr = OSCARS::PluginManager->new();
my $authN = $mgr->usePlugin('authentication');

my $credentials = $authN->getCredentials($login, 'password');
my $user = $authN->getUser($login);
ok($user);
