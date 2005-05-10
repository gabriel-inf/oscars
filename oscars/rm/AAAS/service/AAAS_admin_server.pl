#!/usr/bin/perl -w

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;

use AAAS::Frontend::Admin;

our( $config, $dbAdmin);

$config = Config::Auto::parse('AAAS.config');

$dbAdmin = AAAS::Frontend::Admin->new('configs' => $config);

my $admin_daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'server_admin_port'})
  -> dispatch_to($dbAdmin)
;

$admin_daemon->handle;

