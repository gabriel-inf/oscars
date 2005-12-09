#!/usr/bin/perl -w

use strict;
use Test::Simple tests => 1;

use OSCARS::AAAS::Notifications;

my $notifications = OSCARS::AAAS::Notifications->new();
ok($notifications);
