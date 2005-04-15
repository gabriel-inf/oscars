#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 2000)
  -> dispatch_to('AAASServer')
;

$daemon->handle;

package AAASServer;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(process_user_login get_user_profile set_user_profile );

use AAAS::Frontend::User;
use AAAS::Frontend::Admin;

sub process_user_login {
  my ($class, %params) = @_;
  return (AAAS::Frontend::User::process_login(\%params));
}


sub get_user_profile {
  my ($class, %params) = @_;
  return (get_profile(\%params));
}


sub set_user_profile {
  my ($class, %params) = @_;
  return (set_profile(\%params));
}


