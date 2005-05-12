#!/usr/bin/perl -w

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;

my $config = Config::Auto::parse('AAAS.config');

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'server_port'})
  -> dispatch_to('AAAS_Dispatcher')
;


# dispatches to user or admin, depending on method call

package AAAS_Dispatcher;

use AAAS::Frontend::User;
use AAAS::Frontend::Admin;

my $dispatch_config = Config::Auto::parse('AAAS.config');
my $dbUser = AAAS::Frontend::User->new('configs' => $dispatch_config);
my $dbAdmin = AAAS::Frontend::Admin->new('configs' => $dispatch_config);

# user

sub verify_login
{
   my ($class, $params) = @_;
   $dbUser->verify_login($params);
}

sub get_profile
{
   my ($class, $params, $fields_to_display) = @_;
   $dbUser->get_profile($params, $fields_to_display);
}

sub set_profile
{
   my ($class, $params) = @_;
   $dbUser->set_profile($params);
}

sub logout
{
   my ($class) = @_;
   $dbUser->logout();
}

# admin

sub get_userlist
{
   my ($class, $params) = @_;
   $dbAdmin->get_userlist($params);
}


$daemon->handle;


