#!/usr/bin/perl -w

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;

use AAAS::Frontend::User;

my $config = Config::Auto::parse('AAAS.config');

my $dbUser = AAAS::Frontend::User->new('configs' => $config);

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'server_port'})
  -> dispatch_to($dbUser)
  -> handle;
