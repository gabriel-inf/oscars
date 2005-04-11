#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 4000)
  -> dispatch_to('AAASServer')
;

$daemon->handle;

package AAASServer;

use lib '../..';

our @ISA = qw(Exporter);
our @EXPORT = qw(login);

use AAAS::Frontend::User;

sub login {
  my ($class, $loginname, $password) = @_;
  return (process_user_login($loginname, $password));
}
