#!/usr/bin/perl -w

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;

use AAAS::Frontend::User;

my $config = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');

my $dbUser = AAAS::Frontend::User->new('configs' => $config);

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'AAAS_server_port'})
  -> dispatch_to($dbUser)
  -> handle;
