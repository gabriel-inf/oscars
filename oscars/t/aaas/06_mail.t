#!/usr/bin/perl

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::AAAS::Mail;

my $mailer = OSCARS::AAAS::Mail->new();
my $subject = 'test';
my $sender = $mailer->get_webmaster();
my $msg = "This is a test.\n";

my $err_msg =
       $mailer->send_mail($sender, $mailer->get_admins(), $subject, $msg); 
ok(!$err_msg, $err_msg);
