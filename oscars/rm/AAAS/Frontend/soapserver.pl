#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 2000)
  -> dispatch_to('AAASServer')
;

$daemon->handle;

BEGIN {
package AAASServer;

require 'login.pl';

sub login {
  my ($class, $loginname, $passwd) = @_;
  return (Process_User_Login($loginname, $passwd));
}

}
