#!/usr/bin/perl -w

use strict;
use Test qw(plan ok skip);

plan tests => 1;

use OSCARS::AAAS::Notifications;

$notifications = OSCARS::AAAS::Notifications->new();
ok($notifications);
