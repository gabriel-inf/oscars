#!/usr/bin/perl -w

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;

use lib '../..';
use AAAS::Frontend::User;

our( $config, $dbUser);

$config = Config::Auto::parse('AAAS.config');

$dbUser = AAAS::Frontend::User->new('configs' => $config);

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'server_port'})
  -> dispatch_to($dbUser)
;

$daemon->handle;


