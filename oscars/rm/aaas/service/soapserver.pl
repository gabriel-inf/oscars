#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 2000)
  -> dispatch_to('AAASServer')
;

$daemon->handle;

BEGIN {
package AAASServer;

sub login {
  my ($class, $f) = @_;
  $login_status = "Success";
  return $login_status;
}
}
