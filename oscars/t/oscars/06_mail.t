#!/usr/bin/perl

use strict;
use Test::Simple tests => 2;

use OSCARS::Mail;

my $mailer = OSCARS::Mail->new();
ok($mailer);

my $login = 'testaccount';
my $recipient = $mailer->getWebmaster();
my $results =
    { 'msg' => "This is a test of OSCARS notifications on $ENV{HOST}." };

my $errMsg =
       $mailer->sendMessage($login, 'TestMessage', $results); 
if ($errMsg) { ok(0, $errMsg); }
ok(1);
