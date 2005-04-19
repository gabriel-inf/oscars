#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

use strict;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 4000)
  -> dispatch_to('AAASServer')
;

$daemon->handle;

package AAASServer;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(Verify_login Get_profile Set_profile );

use AAAS::Frontend::User;
use AAAS::Frontend::Admin;

sub Verify_login {
  my ($class, $params) = @_;
  return (verify_login($params));
}


sub Get_profile {
  my ($class, $params, $fields_to_display) = @_;
  return (get_profile($params, @$fields_to_display));
}


sub Set_profile {
  my ($class, %params) = @_;
  return (set_profile(\%params));
}


