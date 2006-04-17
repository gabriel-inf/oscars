#!/usr/bin/perl

use Test::Simple tests => 1;

use OSCARS::PluginManager;

my $user_login = 'testaccount';
my $mgr = OSCARS::PluginManager->new();
my $authN = $mgr->use_plugin('authentication');

my $credentials = $authN->get_credentials($user_login, 'password');
my $user = $authN->get_user($user_login);
ok($user);
