#!/usr/bin/perl -w

use SOAP::Transport::HTTP;

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => 3000)
  -> dispatch_to('BSSServer')
;

$daemon->handle;

BEGIN {
package BSSServer;

require 'reservation.pl';

sub make_reservation {
# FIX, syntax probably wrong
  my ($class, @arglist) = @_;
  return (Process_User_Login(@arglist));
}

}
