#!/usr/bin/perl

use strict;
use Test::Simple tests => 2;

use OSCARS::Mail;

my $mailer = OSCARS::Mail->new();
ok($mailer);

my $sender = $mailer->get_webmaster();
my $recipient = $mailer->get_webmaster();
my $subject = 'test';
my $msg = "This is a test on oscars-dev.\n";

my $err_msg =
       $mailer->send_mail($sender, $recipient, $subject, $msg); 
if ($err_msg) { ok(0, $err_msg); }
else { ok(1, "Successfully sent mail"); }
