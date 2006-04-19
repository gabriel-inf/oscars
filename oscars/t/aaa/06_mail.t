#!/usr/bin/perl

use strict;
use Test::Simple tests => 2;

use OSCARS::Mail;

my $mailer = OSCARS::Mail->new();
ok($mailer);

my $sender = $mailer->getWebmaster();
my $recipient = $mailer->getWebmaster();
my $subject = 'test';
my $msg = "This is a test on oscars-dev.\n";

my $errMsg =
       $mailer->sendMail($sender, $recipient, $subject, $msg); 
if ($errMsg) { ok(0, $errMsg); }
else { ok(1, "Successfully sent mail"); }
